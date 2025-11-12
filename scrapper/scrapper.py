#!/usr/bin/env python3
"""
Async WITS tariff scraper
- concurrency: semaphore-controlled (default 50)
- batch insert size: 200
- uses asyncpg + aiohttp
Environment variables required:
DB_HOST, DB_PORT, DB_USER, DB_PASSWORD, DB_NAME
"""

import os
import asyncio
import aiohttp
import asyncpg
import time
import uuid
import math
import random
from datetime import date, timedelta, datetime
import xml.etree.ElementTree as ET
from typing import Dict, List, Any, Tuple
from dotenv import load_dotenv

load_dotenv()

# DB config env names
DB_HOST = os.getenv("RDS_ENDPOINT", "mypostgreslink")
DB_PORT = os.getenv("RDS_PORT", "5432")
DB_USER = os.getenv("RDS_USERNAME", "postgres")
DB_PASSWORD = os.getenv("RDS_PASSWORD", "postgrespw")
DB_NAME = os.getenv("RDS_DBNAME", "postgres")

# Scraper config
CONCURRENCY = int(os.getenv("CONCURRENCY", "50"))   # you chose 50
PAIRS_PER_FLUSH = int(os.getenv("PAIRS_PER_FLUSH", "10"))  # flush after N complete pairs
MAX_RETRIES = int(os.getenv("MAX_RETRIES", "5"))
RETRY_BASE = float(os.getenv("RETRY_BASE", "0.5"))  # seconds
API_TIMEOUT = int(os.getenv("API_TIMEOUT", "30"))   # per request timeout seconds

TOTAL_ORIGINS_DEST = None  # used only for logging when countries known

# Products list (example list - adjust if needed)
PRODUCT_LIST = [
    "847330",
    "847170",
    "851712",
    "847130",
    "854231"
]

WITS_CODLIST_URL = "https://wits.worldbank.org/API/V1/SDMX/V21/rest/codelist/all/"
WITS_TARIFF_URL_TEMPLATE = (
    "https://wits.worldbank.org/API/V1/SDMX/V21/rest/data/DF_WITS_Tariff_TRAINS/"
    "A.{destination}.{origin}.{product}.reported/?startperiod=1988&detail=dataOnly"
)

# SQL statements (lowercase table names, snake_case columns)
CREATE_TEMP_TABLE_SQL = """
CREATE TEMP TABLE incoming_tariffs (
    temp_id UUID,
    origin_country_code TEXT,
    dest_country_code TEXT,
    effective_date DATE,
    expiry_date DATE,
    ad_valorem_rate NUMERIC,
    specific_rate NUMERIC,
    min_quantity INTEGER,
    max_quantity INTEGER,
    user_defined BOOLEAN,
    enabled BOOLEAN,
    hts_code TEXT
) ON COMMIT DROP
"""

INSERT_TEMP_TABLE_SQL = """
INSERT INTO incoming_tariffs VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12)
"""

SELECT_EXISTING_SQL="""
SELECT 
    i.temp_id,
    t.id as existing_id,
    t.ad_valorem_rate as existing_ad_valorem,
    t.specific_rate as existing_specific,
    i.ad_valorem_rate as new_ad_valorem,
    i.specific_rate as new_specific
FROM incoming_tariffs i
INNER JOIN tariff t 
    ON t.origin_country_code = i.origin_country_code
    AND t.dest_country_code = i.dest_country_code
    AND t.effective_date = i.effective_date
    AND COALESCE(t.expiry_date, '9999-12-31'::date) = COALESCE(i.expiry_date, '9999-12-31'::date)
INNER JOIN tariff_product tp 
    ON tp.tariff_id = t.id 
    AND tp.hts_code = i.hts_code
"""

UPDATE_EXISTING_SQL="""
UPDATE tariff t
SET ad_valorem_rate = i.ad_valorem_rate,
    specific_rate = i.specific_rate,
    min_quantity = i.min_quantity,
    max_quantity = i.max_quantity,
    user_defined = i.user_defined
FROM incoming_tariffs i
INNER JOIN tariff_product tp ON tp.hts_code = i.hts_code
WHERE t.id = tp.tariff_id
    AND t.origin_country_code = i.origin_country_code
    AND t.dest_country_code = i.dest_country_code
    AND t.effective_date = i.effective_date
    AND COALESCE(t.expiry_date, '9999-12-31'::date) = COALESCE(i.expiry_date, '9999-12-31'::date)
    AND t.id = ANY($1::uuid[])
"""

TARIFF_INSERT_SQL = """
INSERT INTO tariff(
    id, origin_country_code, dest_country_code, effective_date, expiry_date,
    ad_valorem_rate, specific_rate, min_quantity, max_quantity, user_defined, enabled
)
VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10, $11)
RETURNING id
"""

TARIFF_PRODUCT_INSERT_SQL = """
INSERT INTO tariff_product(tariff_id, hts_code)
VALUES ($1, $2)
ON CONFLICT (tariff_id, hts_code) DO NOTHING;
"""


# Helper: XML parsing functions (adapted from your original impl)
def extract_country_codes_and_names(xml_content: bytes) -> Dict[str, str]:
    root = ET.fromstring(xml_content)
    namespace = {
        'ns': 'http://www.sdmx.org/resources/sdmxml/schemas/v2_1/message',
        'structure': 'http://www.sdmx.org/resources/sdmxml/schemas/v2_1/structure',
        'common': 'http://www.sdmx.org/resources/sdmxml/schemas/v2_1/common'
    }
    codelist = root.find(".//structure:Codelist[@id='CL_COUNTRY_WITS']", namespace)
    if codelist is None:
        print("Codelist with id 'CL_COUNTRY_WITS' not found.")
        return {}
    country_dict = {}
    for code in codelist.findall("structure:Code", namespace):
        code_id = code.attrib.get('id')
        if not code_id:
            continue
        # your original filter: skip codes where first char is not digit
        # but that seems odd for country codes; keep original behaviour:
        if not code_id[0].isdigit():
            continue
        name_element = code.find("common:Name", namespace)
        country_name = name_element.text if name_element is not None else "Unknown"
        country_dict[code_id] = country_name
    return country_dict

def parse_tariff_from_content(xml_content: bytes, origin_country: str, destination_country: str) -> Dict[str, List[dict]]:
    root = ET.fromstring(xml_content)
    namespace = {
        'generic': 'http://www.sdmx.org/resources/sdmxml/schemas/v2_1/data/generic',
        'message': 'http://www.sdmx.org/resources/sdmxml/schemas/v2_1/message'
    }
    records_by_product: Dict[str, List[dict]] = {}
    series_list = root.findall(".//generic:Series", namespace)
    if not series_list:
        return records_by_product
    for series in series_list:
        key = series.find("generic:SeriesKey/generic:Value[@id='PRODUCTCODE']", namespace)
        if key is None:
            continue
        product_code = key.attrib.get("value")
        if not product_code:
            continue
        records_by_product.setdefault(product_code, [])
        for obs in series.findall("generic:Obs", namespace):
            time_dimension = obs.find("generic:ObsDimension[@id='TIME_PERIOD']", namespace)
            value_element = obs.find("generic:ObsValue", namespace)
            if time_dimension is None or value_element is None:
                continue
            time_period = time_dimension.attrib.get("value")
            ad_valorem_rate = value_element.attrib.get("value")
            try:
                eff_date = date(int(time_period), 1, 1)
            except Exception:
                continue
            try:
                ad_rate = float(ad_valorem_rate) * 0.01
            except Exception:
                ad_rate = 0.0
            rec = {
                "id": str(uuid.uuid4()),
                "origin_country": origin_country,
                "destination_country": destination_country,
                "effective_date": eff_date,
                "expiry_date": None,
                "ad_valorem_rate": ad_rate,
                "specific_rate": 0.0,
                "min_quantity": 0,
                "max_quantity": 0,
                "user_defined": False,
                "enabled": True
            }
            records_by_product[product_code].append(rec)
    return records_by_product

def format_rec(records):
    for r in records:
        print(f"{r["effective_date"]}, {r["expiry_date"]}, {r["ad_valorem_rate"]}, {r["specific_rate"]}")

def clean_tariff(records: List[dict]) -> List[dict]:
    if not records:
        return []
    records.sort(key=lambda x: x["effective_date"])
    result = []
    curr = records[0]
    for record in records[1:]:
        if not (
            math.isclose(record["ad_valorem_rate"], curr["ad_valorem_rate"], rel_tol=1e-9)
            and math.isclose(record["specific_rate"], curr["specific_rate"], rel_tol=1e-9)
            and record["min_quantity"] == curr["min_quantity"]
            and record["max_quantity"] == curr["max_quantity"]
        ):
            # set expiry to day before next effective
            curr["expiry_date"] = record["effective_date"] - timedelta(days=1)
            result.append(curr)
            curr = record
    result.append(curr)
    return result

# Async fetch helpers
async def fetch_bytes_with_retries(session: aiohttp.ClientSession, url: str, semaphore: asyncio.Semaphore) -> bytes | None:
    backoff = RETRY_BASE
    txt = url.split(".")
    for attempt in range(1, MAX_RETRIES + 1):
        async with semaphore:
            try:
                async with session.get(url, timeout=API_TIMEOUT) as resp:
                    if resp.status == 200:
                        return await resp.read()
                    elif resp.status in (429, 503):
                        # transient: retry
                        pass
                    elif resp.status == 404:
                        print(f"Not found for dest: {txt[3]}, ori: {txt[4]}")
                        return None
                    else:
                        return None
                    
            except (aiohttp.ClientError, asyncio.TimeoutError) as e:
                # treat as transient
                pass
        # jittered backoff
        jitter = random.random() * 0.1 * backoff
        await asyncio.sleep(backoff + jitter)
        backoff *= 2
    print(f"Max retries exceeded for dest: {txt[3]}, ori: {txt[4]}")
    return None

async def fetch_country_codes_async(session: aiohttp.ClientSession, semaphore: asyncio.Semaphore) -> Dict[str, str]:
    data = await fetch_bytes_with_retries(session, WITS_CODLIST_URL, semaphore)
    if not data:
        return {}
    return extract_country_codes_and_names(data)

async def fetch_tariff_for_pair(session: aiohttp.ClientSession, semaphore: asyncio.Semaphore, origin: str, destination: str, products: List[str]) -> Dict[str, List[dict]] | None:
    product_join = "+".join(products)
    url = WITS_TARIFF_URL_TEMPLATE.format(origin=origin, destination=destination, product=product_join)
    data = await fetch_bytes_with_retries(session, url, semaphore)
    if not data:
        return None
    return parse_tariff_from_content(data, origin, destination)

# DB flush helpers - FIXED VERSION
async def flush_batches(pool: asyncpg.pool.Pool, tariff_rows: List[Tuple], product_links: Dict[str, str]):
    if not tariff_rows or not product_links:
        return
    
    async with pool.acquire() as conn:
        async with conn.transaction():
            # Create temp table with incoming data
            await conn.execute(CREATE_TEMP_TABLE_SQL)
            
            # Bulk insert into temp table
            temp_rows = []
            for row in tariff_rows:
                temp_id = row[0]
                hts_code = product_links.get(temp_id)
                if hts_code:
                    temp_rows.append((*row, hts_code))
            
            await conn.executemany(INSERT_TEMP_TABLE_SQL, temp_rows)
            
            # Find existing matches with a single JOIN
            existing = await conn.fetch(SELECT_EXISTING_SQL)
            
            # Determine what needs updating
            to_update = []
            existing_temp_ids = set()
            for row in existing:
                existing_temp_ids.add(str(row['temp_id']))
                if (not math.isclose(row['existing_ad_valorem'], row['new_ad_valorem'], rel_tol=1e-9) or
                    not math.isclose(row['existing_specific'], row['new_specific'], rel_tol=1e-9)):
                    to_update.append(row['existing_id'])
            
            # Update existing
            if to_update:
                await conn.execute(UPDATE_EXISTING_SQL, to_update)
                print(f"  Updated {len(to_update)} existing tariffs")
            
            # Insert new records (those not in existing_temp_ids)
            new_tariffs = [row for row in temp_rows if row[0] not in existing_temp_ids]
            if new_tariffs:
                id_mapping = {}
                for row in new_tariffs:
                    result = await conn.fetch(TARIFF_INSERT_SQL, *row[:11])
                    id_mapping[row[0]] = str(result[0]["id"])
                
                # Insert product links
                product_rows = [(id_mapping[row[0]], row[11]) for row in new_tariffs if row[0] in id_mapping]
                if product_rows:
                    await conn.executemany(TARIFF_PRODUCT_INSERT_SQL, product_rows)
                
                print(f"  Inserted {len(new_tariffs)} new tariffs")

# Coordinator: runs the full scraping
async def run_scrape():
    start = time.time()
    semaphore = asyncio.Semaphore(CONCURRENCY)
    db_dsn = f"postgresql://{DB_USER}:{DB_PASSWORD}@{DB_HOST}:{DB_PORT}/{DB_NAME}"
    pool = await asyncpg.create_pool(dsn=db_dsn, min_size=1, max_size=max(2, CONCURRENCY // 4 + 1))
    print(f"Connected to DB pool. concurrency={CONCURRENCY}, pairs_per_flush={PAIRS_PER_FLUSH}")

    timeout = aiohttp.ClientTimeout(total=None, sock_connect=10, sock_read=API_TIMEOUT)
    async with aiohttp.ClientSession(timeout=timeout) as session:
        print("Fetching country codes...")
        country_dict = await fetch_country_codes_async(session, semaphore)
        if not country_dict:
            print("No countries found — exiting.")
            await pool.close()
            return
        countries = list(country_dict.keys())
        total_pairs = len(countries) * (len(countries) - 1)
        print(f"Found {len(countries)} countries => {total_pairs} origin-destination pairs (excluding same-country).")
        global TOTAL_ORIGINS_DEST
        TOTAL_ORIGINS_DEST = total_pairs

        # Atomic pair buffering: collect complete pairs, flush every 10 pairs
        tariff_rows_buffer: List[Tuple] = []
        product_links_buffer: Dict[str, str] = dict() # tariff_id -> hts_code
        pairs_in_buffer = 0  # count complete pairs added to buffer
        processed_pairs = 0
        saved_tariffs = 0
        
        # Lock for atomic buffer operations
        buffer_lock = asyncio.Lock()

        async def handle_pair(origin: str, destination: str):
            nonlocal tariff_rows_buffer, product_links_buffer, pairs_in_buffer, processed_pairs, saved_tariffs
            
            # Fetch data (async, no lock needed)
            records_map = await fetch_tariff_for_pair(session, semaphore, origin, destination, PRODUCT_LIST)
            
            # Process the pair's data into temporary local buffers
            pair_tariff_rows: List[Tuple] = []
            pair_product_links: Dict[str, str] = {}
            
            if records_map:
                for product_code, records in records_map.items():
                    cleaned = clean_tariff(records)
                    for rec in cleaned:
                        # build tariff row tuple in the same column order as TARIFF_INSERT_SQL
                        tariff_row = (
                            rec["id"],
                            rec["origin_country"],
                            rec["destination_country"],
                            rec["effective_date"],
                            rec["expiry_date"],
                            rec["ad_valorem_rate"],
                            rec["specific_rate"],
                            rec["min_quantity"],
                            rec["max_quantity"],
                            rec["user_defined"],
                            rec["enabled"],
                        )
                        pair_tariff_rows.append(tariff_row)
                        pair_product_links[rec["id"]] = product_code
            
            # Atomically add this pair's data to the buffer
            async with buffer_lock:
                processed_pairs += 1
                
                if pair_tariff_rows:
                    # Add entire pair atomically
                    tariff_rows_buffer.extend(pair_tariff_rows)
                    product_links_buffer.update(pair_product_links)
                    pairs_in_buffer += 1
                    saved_tariffs += len(pair_tariff_rows)
                    
                    # Flush when we've accumulated enough complete pairs
                    if pairs_in_buffer >= PAIRS_PER_FLUSH:
                        print(f"Flushing {pairs_in_buffer} pairs ({len(tariff_rows_buffer)} tariffs) to DB...")
                        await flush_batches(pool, tariff_rows_buffer, product_links_buffer)
                        tariff_rows_buffer = []
                        product_links_buffer = {}
                        pairs_in_buffer = 0
                
                # periodic progress log
                if processed_pairs % 100 == 0:
                    print(f"[{processed_pairs}/{total_pairs}] processed. saved_tariffs={saved_tariffs}. pairs_in_buffer={pairs_in_buffer}")

        # Create all pair tasks - dispatch async requests
        pair_list = [(o, d) for o in countries for d in countries if o != d and d != "000"]
        
        # Submit all tasks at once - semaphore controls concurrency
        print(f"Dispatching {len(pair_list)} async requests...")
        tasks = [asyncio.create_task(handle_pair(o, d)) for (o, d) in pair_list]
        await asyncio.gather(*tasks)

        # flush any remaining rows
        async with buffer_lock:
            if tariff_rows_buffer or product_links_buffer:
                print(f"Final flush: {pairs_in_buffer} pairs ({len(tariff_rows_buffer)} tariffs)")
                await flush_batches(pool, tariff_rows_buffer, product_links_buffer)

        await pool.close()
        elapsed = time.time() - start
        print(f"Scrape complete. total pairs processed: {processed_pairs}. total tariffs inserted: ~{saved_tariffs}. elapsed: {elapsed:.1f}s")

# Entrypoint
def main():
    print("=" * 40)
    readable_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")  # Format: YYYY-MM-DD HH:MM:SS
    print(f"Starting scraper (async) at {readable_time}...")
    asyncio.run(run_scrape())

if __name__ == "__main__":
    main()