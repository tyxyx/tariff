"use client";
import { useEffect, useState, useCallback } from "react";
import PageHeader from "@/components/ui/PageHeader";
import { colors } from "@/styles/colors";

export default function HeatmapPage() {
		const [tariffs, setTariffs] = useState([]);
		const [loading, setLoading] = useState(false);
		const [error, setError] = useState(null);

		const [origins, setOrigins] = useState([]);
		const [selectedOrigin, setSelectedOrigin] = useState("");
		const [mode, setMode] = useState("export"); // 'export' = tariffs from origin -> dest, 'import' = tariffs where dest == origin

		// --- caching helpers: try cookie first, fallback to localStorage ---
		const clearTariffsCache = useCallback(() => {
			document.cookie = 'tariffs_cache_v1=; path=/; max-age=0';
			try { localStorage.removeItem('tariffs_cache_v1'); } catch (e) { }
		}, []);

		const getSavedTariffs = useCallback(() => {
			try {
				// cookie name: tariffs_cache_v1
				const match = document.cookie.split('; ').find(r => r.startsWith('tariffs_cache_v1='));
				if (match) {
					const raw = match.split('=')[1] || '';
					if (raw) {
						try { return JSON.parse(decodeURIComponent(raw)); } catch (e) { console.warn('Failed parsing tariffs cookie, clearing cache', e); clearTariffsCache(); }
					}
				}
				const ls = localStorage.getItem('tariffs_cache_v1');
				if (ls) return JSON.parse(ls);
			} catch (e) {
				console.warn('getSavedTariffs error', e);
			}
			return null;
		}, [clearTariffsCache]);

		const saveTariffsToCookie = useCallback((obj) => {
			try {
				const s = JSON.stringify(obj || []);
				const enc = encodeURIComponent(s);
				// write cookie for 30 days
				document.cookie = `tariffs_cache_v1=${enc}; path=/; max-age=${30 * 24 * 60 * 60}`;
				// verify written value (cookie truncation possible)
				const back = (document.cookie.split('; ').find(r => r.startsWith('tariffs_cache_v1=')) || '').split('=')[1] || '';
				if (back && back === enc) return;
				// fallback to localStorage when cookie cannot hold payload
				localStorage.setItem('tariffs_cache_v1', s);
				console.warn('tariffs saved to localStorage because cookie was too small');
			} catch (e) {
				console.warn('saveTariffsToCookie error, falling back to localStorage', e);
				try { localStorage.setItem('tariffs_cache_v1', JSON.stringify(obj || [])); } catch (e2) { /* ignore */ }
			}
		}, []);

	// allow calling helpers from effect without listing them as deps
	// eslint-disable-next-line react-hooks/exhaustive-deps
	useEffect(() => {
			setLoading(true);
			// try cache first
			const cached = getSavedTariffs();
			if (cached && Array.isArray(cached) && cached.length > 0) {
				setTariffs(cached);
				const originNames = Array.from(new Set(cached.map(t => t.originCountry?.name).filter(Boolean))).sort();
				setOrigins(originNames);
				if (originNames.length > 0) setSelectedOrigin(originNames[0]);
				setLoading(false);
				return;
			}

			fetch(`http://${process.env.NEXT_PUBLIC_BACKEND_EC2_HOST}:8080/api/tariffs`)
				.then((res) => {
					if (!res.ok) throw new Error(`Status ${res.status}`);
					return res.json();
				})
				.then((data) => {
					const list = data || [];
					setTariffs(list);
					const originNames = Array.from(new Set(list.map(t => t.originCountry?.name).filter(Boolean))).sort();
					setOrigins(originNames);
					if (originNames.length > 0) setSelectedOrigin(originNames[0]);
					// persist for future reloads
					saveTariffsToCookie(list);
				})
				.catch((e) => setError(e.message || String(e)))
				.finally(() => setLoading(false));
		}, []);

		// Build and download CSV for current filtered view
		const exportFilteredCSV = () => {
			if (!selectedOrigin) return;
			const filtered = tariffs.filter(t => {
				if (mode === 'export') return t.originCountry?.name === selectedOrigin;
				return t.destCountry?.name === selectedOrigin;
			});
			if (!filtered.length) return;
			const rows = filtered.map(t => ({
				id: t.id,
				from: t.originCountry?.name ?? '',
				to: t.destCountry?.name ?? '',
				effectiveDate: t.effectiveDate ?? '',
				adValoremRate: t.adValoremRate ?? '',
				specificRate: t.specificRate ?? '',
				products: (t.products || []).map(p => p.htsCode ?? p.code ?? p.name).join(';')
			}));
			const header = Object.keys(rows[0]);
			const csv = [header.join(',')].concat(rows.map(r => header.map(h => `"${String(r[h] ?? '') .replace(/"/g, '""')}"`).join(','))).join('\n');
			const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
			const url = URL.createObjectURL(blob);
			const a = document.createElement('a');
			a.href = url;
			a.download = `tariffs_${mode}_${selectedOrigin.replace(/\s+/g,'_')}.csv`;
			document.body.appendChild(a);
			a.click();
			a.remove();
			URL.revokeObjectURL(url);
		};

		function FilteredTable({ tariffs, origin, mode }) {
			if (!origin) return <div className="text-gray-400">Select an origin country to view tariffs.</div>;
			const filtered = (tariffs || []).filter(t => mode === 'export' ? t.originCountry?.name === origin : t.destCountry?.name === origin);
			const count = filtered.length;
			const avgAd = (filtered.reduce((s, t) => s + (t.adValoremRate || 0), 0) / Math.max(1, count)).toFixed(2);
			const avgSpec = (filtered.reduce((s, t) => s + (t.specificRate || 0), 0) / Math.max(1, count)).toFixed(2);
			return (
				<div>
					<div className="mb-2 text-sm text-gray-300">Matches: {count} — Avg ad-valorem: {avgAd}% — Avg specific: {avgSpec}</div>
					<div className="overflow-auto">
						<table className="min-w-full text-sm" style={{ borderCollapse: 'collapse' }}>
							<thead>
								<tr>
									<th className="text-left pr-4 pb-2">{mode === 'export' ? 'Destination' : 'Origin'}</th>
									<th className="text-left pr-4 pb-2">Effective</th>
									<th className="text-left pr-4 pb-2">Ad Valorem %</th>
									<th className="text-left pr-4 pb-2">Specific</th>
									<th className="text-left pr-4 pb-2">Products (HTS)</th>
								</tr>
							</thead>
							<tbody>
								{filtered.map(t => (
									<tr key={t.id} style={{ borderTop: '1px solid rgba(255,255,255,0.05)' }}>
										<td className="pr-4 py-2">{mode === 'export' ? (t.destCountry?.name ?? '-') : (t.originCountry?.name ?? '-')}</td>
										<td className="pr-4 py-2">{t.effectiveDate ?? '-'}</td>
										<td className="pr-4 py-2">{t.adValoremRate ?? '-'}</td>
										<td className="pr-4 py-2">{t.specificRate ?? '-'}</td>
										<td className="pr-4 py-2">{(t.products || []).map(p => p.htsCode ?? p.code ?? p.name).slice(0,5).join(', ')}{(t.products || []).length > 5 ? '…' : ''}</td>
									</tr>
								))}
							</tbody>
						</table>
					</div>
				</div>
			);
		}

	return (
		<div style={{ backgroundColor: colors.background, color: colors.text, minHeight: "100vh" }}>
			<PageHeader />
			<div className="container mx-auto px-6 py-8">
				<h2 className="text-2xl font-bold mb-4">Tariff data preview</h2>

						{loading && <div>Loading tariffs...</div>}
						{error && <div className="text-red-400">Error: {error}</div>}

						{!loading && !error && (
							<div>
								<div className="mb-4 flex items-center gap-4">
									<label className="block font-medium">Origin country</label>
									<select
										className="border rounded px-3 py-1 bg-black text-white"
										value={selectedOrigin}
										onChange={e => setSelectedOrigin(e.target.value)}
									>
										{origins.map(o => <option key={o} value={o}>{o}</option>)}
									</select>

									<div className="ml-4 flex items-center space-x-2">
										<button
											className={`px-3 py-1 rounded ${mode === 'export' ? 'bg-blue-600 text-white' : 'bg-gray-800 text-gray-300'}`}
											onClick={() => setMode('export')}
										>Exporting from</button>
										<button
											className={`px-3 py-1 rounded ${mode === 'import' ? 'bg-blue-600 text-white' : 'bg-gray-800 text-gray-300'}`}
											onClick={() => setMode('import')}
										>Importing to</button>
									</div>

									<div className="ml-auto">
										<button
											className="px-3 py-1 bg-green-600 text-white rounded"
											onClick={() => exportFilteredCSV()}
											disabled={!selectedOrigin}
										>Export CSV</button>
									</div>
								</div>

								<FilteredTable tariffs={tariffs} origin={selectedOrigin} mode={mode} />
							</div>
						)}
			</div>
		</div>
	);
}

