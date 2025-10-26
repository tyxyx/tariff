
import psycopg2
import sys
import boto3
import os
import requests
import xml.etree.ElementTree as ET
import time
import uuid
from datetime import date, timedelta

from dotenv import load_dotenv

load_dotenv()

# AWS
RDS_ENDPOINT=os.getenv("RDS_ENDPOINT")
RDS_PORT=os.getenv("RDS_PORT")
RDS_USERNAME=os.getenv("RDS_USERNAME")
RDS_REGION=os.getenv("RDS_REGION")
RDS_DBNAME=os.getenv("RDS_DBNAME")

# LOCAL
LOCAL_ENDPOINT=os.getenv("LOCAL_ENDPOINT")
LOCAL_PORT=os.getenv("LOCAL_PORT")
LOCAL_USERNAME=os.getenv("LOCAL_USERNAME")
LOCAL_DBNAME=os.getenv("LOCAL_DBNAME")
LOCAL_PASSWORD=os.getenv("LOCAL_PASSWORD")


# try:
#     # AWS
#     conn = psycopg2.connect(host=ENDPOINT, port=PORT, database=DBNAME, user=USERNAME, password=token, sslrootcert="SSLCERTIFICATE")
    
#     # LOCAL
#     # conn = psycopg2.connect(host=ENDPOINT, port=PORT, database=DBNAME, user=USERNAME, password=PASSWORD)

#     cur = conn.cursor()
#     cur.execute("""SELECT * FROM Users""")
# #     cur.execute("""CREATE USER pgadmin_user WITH PASSWORD 'r007_U53r_PA55w0rd';
# # GRANT ALL PRIVILEGES ON DATABASE postgres TO pgadmin_user;""")
#     query_results = cur.fetchall()
#     print(query_results)
# except Exception as e:
#     print("Database connection failed due to {}".format(e))

def connect_postgres(local: bool=True):
    if local:
        return psycopg2.connect(host=LOCAL_ENDPOINT, port=LOCAL_PORT, database=LOCAL_DBNAME, user=LOCAL_USERNAME, password=LOCAL_PASSWORD)
    else:
        session = boto3.Session(profile_name='RDSCreds', region_name=RDS_REGION) #gets the credentials from .aws/credentials
        client = session.client('rds')
        token = client.generate_db_auth_token(DBHostname=RDS_ENDPOINT, Port=RDS_PORT, DBUsername=RDS_USERNAME, Region=RDS_REGION)
        psycopg2.connect(host=RDS_ENDPOINT, port=RDS_PORT, database=RDS_DBNAME, user=RDS_USERNAME, password=token, sslrootcert="SSLCERTIFICATE")

# TODO: change to aws compatible connect_postgres
def write_countries(new_data: dict[str, str]):
    conn = connect_postgres(True)
    try:
        cur = conn.cursor()
        sql = """INSERT INTO country(code, name)
VALUES (%s, %s)
ON CONFLICT (code) DO UPDATE
SET name = EXCLUDED.name;"""
        for code, name in new_data.items():
            cur.execute(sql, (code, name))
        conn.commit()
    except Exception as e:
        print(f"Failed to insert countries", e)
    finally:
      conn.close()
    
def extract_country_codes_and_names(xml_content):
    """
    Extracts country codes and names from the given XML file.

    Args:
        xml_file (str): Path to the XML file.

    Returns:
        dict: A dictionary where the keys are country codes and the values are country names.
    """
    root = ET.fromstring(xml_content)

    # Define the namespace used in the XML file
    namespace = {
        'ns': 'http://www.sdmx.org/resources/sdmxml/schemas/v2_1/message',
        'structure': 'http://www.sdmx.org/resources/sdmxml/schemas/v2_1/structure',
        'common': 'http://www.sdmx.org/resources/sdmxml/schemas/v2_1/common'
    }

    # Find the Codelist with id="CL_COUNTRY_WITS"
    codelist = root.find(".//structure:Codelist[@id='CL_COUNTRY_WITS']", namespace)
    if codelist is None:
        print("Codelist with id 'CL_COUNTRY_WITS' not found.")
        return {}

    # Extract all country codes and names
    country_dict = {}
    for code in codelist.findall("structure:Code", namespace):
        code_id = code.attrib['id']
        if not code_id[0].isdigit():
            continue
        name_element = code.find("common:Name", namespace)
        country_name = name_element.text if name_element is not None else "Unknown"
        country_dict[code_id] = country_name

    return country_dict

def fetch_country_codes():
    """
    Fetches a dictionary of countries and their codes from the World Bank API.

    Returns:
        dict: A dictionary where the keys are country codes and the values are country names.
    """
    url = "https://wits.worldbank.org/API/V1/SDMX/V21/rest/codelist/all/"
    
    try:
        response = requests.get(url)
        response.raise_for_status()  # Raise an HTTPError for bad responses (4xx and 5xx)
        return extract_country_codes_and_names(response.content)
    except requests.exceptions.RequestException as e:
        print(f"API request failed: {e}")
        return None

def parse_tariff_from_content(xml_content: bytes, origin_country: str, destination_country: str) -> dict[str, list[dict]]:
    """
    Parses the XML content and returns a dictionary of lists of tariff records grouped by product codes.

    Args:
        xml_content (bytes): XML content as bytes.
        origin_country (str): Origin country code.
        destination_country (str): Destination country code.

    Returns:
        dict[str, list[dict]]: Dictionary where keys are product codes and values are lists of tariff records.
    """
    root = ET.fromstring(xml_content)

    namespace = {
        'generic': 'http://www.sdmx.org/resources/sdmxml/schemas/v2_1/data/generic',
        'message': 'http://www.sdmx.org/resources/sdmxml/schemas/v2_1/message'
    }

    records_by_product = {}
    series_list = root.findall(".//generic:Series", namespace)
    if not series_list:
        print("No series found in the XML content.")
        return records_by_product

    for series in series_list:
        product_code = series.find("generic:SeriesKey/generic:Value[@id='PRODUCTCODE']", namespace).attrib.get("value")
        if product_code not in records_by_product:
            records_by_product[product_code] = []

        for obs in series.findall("generic:Obs", namespace):
            time_period = obs.find("generic:ObsDimension[@id='TIME_PERIOD']", namespace).attrib.get("value")
            ad_valorem_rate = obs.find("generic:ObsValue", namespace).attrib.get("value")

            record = {
                "id": str(uuid.uuid4()),
                "originCountry": origin_country,
                "destinationCountry": destination_country,
                "effectiveDate": date(int(time_period), 1, 1),
                "expiryDate": None,  # Assuming no expiry date is provided in the XML
                "adValoremRate": float(ad_valorem_rate) * 0.01,
                "specificRate": 0.0,  # Assuming no specific rate is provided in the XML
                "minQuantity": 0,  # Assuming no minimum quantity is provided in the XML
                "maxQuantity": 0,  # Assuming no maximum quantity is provided in the XML
                "userDefined": False  # Assuming the record is not user-defined
            }
            records_by_product[product_code].append(record)

    return records_by_product

def fetch_tariff(origin, destination, products=["847130", "847170", "847330", "851712", "854231"]):
    url = "https://wits.worldbank.org/API/V1/SDMX/V21/rest/data/DF_WITS_Tariff_TRAINS/A.{destination}.{origin}.{product}.reported/?startperiod=1988&detail=dataOnly".format(origin=origin, destination=destination, product="+".join(products))
    
    try:
        response = requests.get(url)
        response.raise_for_status()  # Raise an HTTPError for bad responses (4xx and 5xx)
        records = parse_tariff_from_content(response.content, origin, destination)
        return records
    except requests.exceptions.RequestException as e:
        print(f"API request failed: {e}")
        return None

def clean_tariff(records):
    result = []
    curr = records[0]
    # Sort the records list in place by effectiveDate
    records.sort(key=lambda x: x["effectiveDate"])
    for record in records[1:]:
        if not (record["adValoremRate"] == curr["adValoremRate"] and \
        record["specificRate"] == curr["specificRate"] and \
        record["minQuantity"] == curr["minQuantity"] and \
        record["maxQuantity"] == curr["maxQuantity"]):
            curr["expiryDate"] = record["effectiveDate"] - timedelta(days=1)
            result.append(curr)
            curr = record
    result.append(curr)
    return result


def write_tariff(record: dict, HTSCode: str) -> int:
    """
    Inserts a single tariff record into the database and returns the ID of the inserted record.

    Args:
        record (dict): A dictionary containing tariff record data.
        HTSCode (str): The product code associated with the tariff.

    Returns:
        int: The ID of the inserted record, or None if the insertion failed.
    """
    conn = connect_postgres(True)
    cur = conn.cursor()
    insert_sql = """INSERT INTO tariff(id, "originCountryCode", "destCountryCode", "effectiveDate", "expiryDate", "adValoremRate", "specificRate", "minQuantity", "maxQuantity", "userDefined")
VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
RETURNING id;"""  # Use RETURNING to fetch the ID of the inserted record

    search_sql = """SELECT * FROM public.tariff_product, public.tariff
WHERE public.tariff_product."tariffId" = public.tariff.id
AND "originCountryCode" = %s 
AND "destCountryCode" = %s 
AND "effectiveDate" = %s 
AND "expiryDate" = %s 
AND "HTSCode" = %s"""

    update_sql = """UPDATE tariff
SET
    "adValoremRate" = %s,
    "specificRate" = %s,
    "minQuantity" = %s,
    "maxQuantity" = %s,
    "userDefined" = %s
WHERE
    "originCountryCode" = %s
    AND "destCountryCode" = %s
    AND "effectiveDate" = %s
    AND "expiryDate" = %s;
"""

    try:
        cur.execute(search_sql, (
            record["originCountry"],
            record["destinationCountry"],
            record["effectiveDate"],
            record["expiryDate"],
            HTSCode
        ))
        search_result = cur.fetchone()

        if search_result:
            cur.execute(update_sql, (
              record["adValoremRate"],
              record["specificRate"],
              record["minQuantity"],
              record["maxQuantity"],
              record["userDefined"],
              record["originCountry"],
              record["destinationCountry"],
              record["effectiveDate"],
              record["expiryDate"],
            ))
            conn.commit()
            return
      
        cur.execute(insert_sql, (
            str(record["id"]),
            record["originCountry"],
            record["destinationCountry"],
            record["effectiveDate"],
            record["expiryDate"],
            record["adValoremRate"],
            record["specificRate"],
            record["minQuantity"],
            record["maxQuantity"],
            record["userDefined"]
        ))

        # Fetch the ID of the inserted record
        inserted_id = cur.fetchone()
        if inserted_id:
            link_sql = """INSERT INTO tariff_product("HTSCode", "tariffId")
            VALUES (%s, %s)
            ON CONFLICT ("HTSCode", "tariffId") DO NOTHING"""
            cur.execute(link_sql, (HTSCode, inserted_id[0]))
            conn.commit()
        return inserted_id[0] if inserted_id else None
    except Exception as e:
        print(f"Failed to insert tariff record: {e}")
        return None
    finally:
        conn.close()

start_time = time.time()

product_list = [
    "847330",
    "847170",
    "851712",
    "847130",
    "854231"
]

print("Retrieving country information")
country_dict = fetch_country_codes()
print("writing to db")
write_countries(country_dict)
print("Countries information saved to db\n")

# TODO: remove all breaks
for origin_country_code in country_dict.keys():
    for destination_country_code in country_dict.keys():
        if (destination_country_code == "000"):
            continue
        if origin_country_code != destination_country_code:
            print(f"Fetching data [origin: {origin_country_code}, dest: {destination_country_code}]")
            records = fetch_tariff(origin_country_code, destination_country_code)
            if not records:
                print("No records found")
                continue
            for product_code in records:
                tariffs = clean_tariff(records[product_code])
                print(f"Wrting to db")
                for r in tariffs:
                    write_tariff(r, product_code)
                print(f"Saved tariff [origin: {origin_country_code}, dest: {destination_country_code}, prod: {product_code}]")
    #         break
    # break
print(f"Program completed in {time.time() - start_time:.2f} seconds.")