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
  const [hideExpired, setHideExpired] = useState(false);

  // form / CRUD state
  const [showForm, setShowForm] = useState(false);
  const [editingTariff, setEditingTariff] = useState(null); // null = create
  const [formState, setFormState] = useState({
    origin: "",
    dest: "",
    effectiveDate: "",
    expiryDate: "",
    adValoremRate: "",
    specificRate: "",
    productsRaw: "",
    productsSelected: [], // new: array of selected product codes
  });
  const [isSaving, setIsSaving] = useState(false);
  const [formError, setFormError] = useState(null);

  // --- caching helpers: try cookie first, fallback to localStorage ---
  const clearTariffsCache = useCallback(() => {
    document.cookie = "tariffs_cache_v1=; path=/; max-age=0";
    try {
      localStorage.removeItem("tariffs_cache_v1");
    } catch (e) {}
  }, []);

  const getSavedTariffs = useCallback(() => {
    try {
      // cookie name: tariffs_cache_v1
      const match = document.cookie
        .split("; ")
        .find((r) => r.startsWith("tariffs_cache_v1="));
      if (match) {
        const raw = match.split("=")[1] || "";
        if (raw) {
          try {
            return JSON.parse(decodeURIComponent(raw));
          } catch (e) {
            console.warn("Failed parsing tariffs cookie, clearing cache", e);
            clearTariffsCache();
          }
        }
      }
      const ls = localStorage.getItem("tariffs_cache_v1");
      if (ls) return JSON.parse(ls);
    } catch (e) {
      console.warn("getSavedTariffs error", e);
    }
    return null;
  }, [clearTariffsCache]);

  const saveTariffsToCookie = useCallback((obj) => {
    try {
      const s = JSON.stringify(obj || []);
      const enc = encodeURIComponent(s);
      // write cookie for 30 days
      document.cookie = `tariffs_cache_v1=${enc}; path=/; max-age=${
        30 * 24 * 60 * 60
      }`;
      // verify written value (cookie truncation possible)
      const back =
        (
          document.cookie
            .split("; ")
            .find((r) => r.startsWith("tariffs_cache_v1=")) || ""
        ).split("=")[1] || "";
      if (back && back === enc) return;
      // fallback to localStorage when cookie cannot hold payload
      localStorage.setItem("tariffs_cache_v1", s);
      console.warn(
        "tariffs saved to localStorage because cookie was too small"
      );
    } catch (e) {
      console.warn(
        "saveTariffsToCookie error, falling back to localStorage",
        e
      );
      try {
        localStorage.setItem("tariffs_cache_v1", JSON.stringify(obj || []));
      } catch (e2) {
        /* ignore */
      }
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
      const originNames = Array.from(
        new Set(cached.map((t) => t.originCountry?.name).filter(Boolean))
      ).sort();
      setOrigins(originNames);
      if (originNames.length > 0) setSelectedOrigin(originNames[0]);
      setLoading(false);
      return;
    }

    // fetch from server when no cache
    const fetchFromServer = async () => {
      try {
        setError(null);
        const res = await fetch(
          `http://${process.env.NEXT_PUBLIC_BACKEND_EC2_HOST}:8080/api/tariffs`
        );
        if (!res.ok) throw new Error(`Status ${res.status}`);
        const data = await res.json();
        const list = data || [];
        setTariffs(list);
        const originNames = Array.from(
          new Set(list.map((t) => t.originCountry?.name).filter(Boolean))
        ).sort();
        setOrigins(originNames);
        if (originNames.length > 0) setSelectedOrigin(originNames[0]);
        saveTariffsToCookie(list);
      } catch (e) {
        setError(e.message || String(e));
      } finally {
        setLoading(false);
      }
    };

    fetchFromServer();
  }, []);

  // refresh helper used by the "Refresh cache" button
  const refreshCacheAndFetch = useCallback(async () => {
    clearTariffsCache();
    setLoading(true);
    setError(null);
    try {
      const res = await fetch(
        `http://${process.env.NEXT_PUBLIC_BACKEND_EC2_HOST}:8080/api/tariffs`
      );
      if (!res.ok) throw new Error(`Status ${res.status}`);
      const data = await res.json();
      const list = data || [];
      setTariffs(list);
      const originNames = Array.from(
        new Set(list.map((t) => t.originCountry?.name).filter(Boolean))
      ).sort();
      setOrigins(originNames);
      if (originNames.length > 0) setSelectedOrigin(originNames[0]);
      saveTariffsToCookie(list);
    } catch (e) {
      setError(e.message || String(e));
    } finally {
      setLoading(false);
    }
  }, [clearTariffsCache, saveTariffsToCookie]);

  // --- CRUD helpers ---

  const openCreateForm = () => {
    setEditingTariff(null);
    setFormError(null);
    setFormState({
      origin: selectedOrigin || "",
      dest: "",
      effectiveDate: "",
      expiryDate: "",
      adValoremRate: "",
      specificRate: "",
      productsRaw: "",
      productsSelected: [],
    });
    setShowForm(true);
  };

  const openEditForm = (t) => {
    setEditingTariff(t);
    setFormError(null);
    const productCodes = (t.products || [])
      .map((p) => p.htsCode ?? p.code ?? p.name)
      .filter(Boolean);
    setFormState({
      origin: t.originCountry?.name ?? "",
      dest: t.destCountry?.name ?? "",
      effectiveDate: t.effectiveDate ?? "",
      expiryDate: t.expiryDate ?? t.endDate ?? t.validUntil ?? "",
      adValoremRate: t.adValoremRate ?? "",
      specificRate: t.specificRate ?? "",
      productsRaw: productCodes.join(", "),
      productsSelected: productCodes,
    });
    setShowForm(true);
  };

  const closeForm = () => {
    setShowForm(false);
    setEditingTariff(null);
    setFormError(null);
  };

  const submitForm = async (e) => {
    e && e.preventDefault();
    setFormError(null);
    // basic validation
    if (!formState.origin || !formState.dest) {
      setFormError("Origin and destination are required.");
      return;
    }

    // prefer selected products array; fallback to productsRaw string (comma separated)
    const selectedProducts =
      (formState.productsSelected && formState.productsSelected.slice()) ||
      (formState.productsRaw || "")
        .split(/[,;]+/)
        .map((s) => s.trim())
        .filter(Boolean);

    const payload = {
      originCountry: { name: formState.origin },
      destCountry: { name: formState.dest },
      effectiveDate: formState.effectiveDate || null,
      expiryDate: formState.expiryDate || null,
      adValoremRate:
        formState.adValoremRate === "" ? null : Number(formState.adValoremRate),
      specificRate:
        formState.specificRate === "" ? null : Number(formState.specificRate),
      products: selectedProducts.map((code) => ({ code })),
    };

    try {
      setIsSaving(true);
      const base = `http://${process.env.NEXT_PUBLIC_BACKEND_EC2_HOST}:8080/api/tariffs`;
      let res;
      if (editingTariff && editingTariff.id) {
        res = await fetch(`${base}/${editingTariff.id}`, {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(payload),
        });
      } else {
        res = await fetch(base, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(payload),
        });
      }
      if (!res.ok) {
        const txt = await res.text().catch(() => null);
        throw new Error(txt || `Status ${res.status}`);
      }
      const saved = await res.json().catch(() => null);

      // update local state: if backend returned saved object, merge; else refetch
      if (saved && saved.id) {
        const next = tariffs.slice();
        if (editingTariff && editingTariff.id) {
          const idx = next.findIndex((x) => x.id === editingTariff.id);
          if (idx >= 0) next[idx] = saved;
        } else {
          next.unshift(saved);
        }
        setTariffs(next);
        const originNames = Array.from(
          new Set(next.map((t) => t.originCountry?.name).filter(Boolean))
        ).sort();
        setOrigins(originNames);
        if (!selectedOrigin && originNames.length) setSelectedOrigin(originNames[0]);
        saveTariffsToCookie(next);
      } else {
        // fallback: refresh from server
        await refreshCacheAndFetch();
      }

      closeForm();
    } catch (err) {
      setFormError(err.message || String(err));
    } finally {
      setIsSaving(false);
    }
  };

  const deleteTariff = async (t) => {
    if (!t?.id) return;
    if (!confirm("Delete this tariff? This cannot be undone.")) return;
    try {
      setLoading(true);
      const res = await fetch(
        `http://${process.env.NEXT_PUBLIC_BACKEND_EC2_HOST}:8080/api/tariffs/${t.id}`,
        { method: "DELETE" }
      );
      if (!res.ok) throw new Error(`Status ${res.status}`);
      const next = tariffs.filter((x) => x.id !== t.id);
      setTariffs(next);
      const originNames = Array.from(
        new Set(next.map((x) => x.originCountry?.name).filter(Boolean))
      ).sort();
      setOrigins(originNames);
      if (selectedOrigin && !originNames.includes(selectedOrigin)) {
        setSelectedOrigin(originNames[0] ?? "");
      }
      saveTariffsToCookie(next);
    } catch (err) {
      setError(err.message || String(err));
    } finally {
      setLoading(false);
    }
  };

  // Build and download CSV for current filtered view
  const exportFilteredCSV = () => {
    if (!selectedOrigin) return;
    let filtered = tariffs.filter((t) => {
      if (mode === "export") return t.originCountry?.name === selectedOrigin;
      return t.destCountry?.name === selectedOrigin;
    });

    // if user chose to hide expired, filter them out from CSV as well
    if (hideExpired) {
      const now = Date.now();
      filtered = filtered.filter((t) => {
        const expiryRaw = t.expiryDate ?? t.endDate ?? t.validUntil ?? null;
        if (!expiryRaw) return true; // no expiry -> consider active
        const d = new Date(expiryRaw);
        if (isNaN(d)) return true;
        return d.getTime() >= now;
      });
    }

    // use same sorting as the table: by country column, active before expired, then expiry (most recent first)
    const isExpiredLocal = (t) => {
      const expiryRaw = t.expiryDate ?? t.endDate ?? t.validUntil ?? null;
      if (!expiryRaw) return false;
      const d = new Date(expiryRaw);
      if (isNaN(d)) return false;
      return d.getTime() < Date.now();
    };
    const getCountryNameLocal = (t) =>
      mode === "export"
        ? t.destCountry?.name ?? ""
        : t.originCountry?.name ?? "";
    const expiryTimeLocal = (t) => {
      const expiryRaw = t.expiryDate ?? t.endDate ?? t.validUntil ?? null;
      if (!expiryRaw) return Infinity;
      const d = new Date(expiryRaw);
      if (isNaN(d)) return Infinity;
      return d.getTime();
    };
    filtered = filtered.slice().sort((a, b) => {
      const aName = (getCountryNameLocal(a) || "").toLowerCase();
      const bName = (getCountryNameLocal(b) || "").toLowerCase();
      if (aName !== bName) return aName.localeCompare(bName);

      const aExp = isExpiredLocal(a);
      const bExp = isExpiredLocal(b);
      if (aExp !== bExp) return aExp ? 1 : -1; // active first

      const aTime = expiryTimeLocal(a);
      const bTime = expiryTimeLocal(b);
      if (isFinite(aTime) && isFinite(bTime)) return bTime - aTime;
      if (!isFinite(aTime) && !isFinite(bTime)) return 0;
      if (!isFinite(aTime)) return -1;
      if (!isFinite(bTime)) return 1;
      return 0;
    });

    if (!filtered.length) return;
    const rows = filtered.map((t) => ({
      from: t.originCountry?.name ?? "",
      to: t.destCountry?.name ?? "",
      effectiveDate: t.effectiveDate ?? "",
      expiryDate: t.expiryDate ?? t.endDate ?? t.validUntil ?? "",
      adValoremRate: t.adValoremRate ?? "",
      specificRate: t.specificRate ?? "",
      products: (t.products || [])
        .map((p) => p.htsCode ?? p.code ?? p.name)
        .join(";"),
    }));
    const header = Object.keys(rows[0]);
    const csv = [header.join(",")]
      .concat(
        rows.map((r) =>
          header
            .map((h) => `"${String(r[h] ?? "").replace(/"/g, '""')}"`)
            .join(",")
        )
      )
      .join("\n");
    const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `tariffs_${mode}_${selectedOrigin.replace(/\s+/g, "_")}.csv`;
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
  };

  function FilteredTable({ tariffs, origin, mode }) {
    if (!origin)
      return (
        <div className="text-gray-400">
          Select an origin country to view tariffs.
        </div>
      );
    let filtered = (tariffs || []).filter((t) =>
      mode === "export"
        ? t.originCountry?.name === origin
        : t.destCountry?.name === origin
    );

    // helper to determine expiry (treat missing/invalid expiry as active)
    const isExpired = (t) => {
      const expiryRaw = t.expiryDate ?? t.endDate ?? t.validUntil ?? null;
      if (!expiryRaw) return false;
      const d = new Date(expiryRaw);
      if (isNaN(d)) return false;
      return d.getTime() < Date.now();
    };
    // hide expired rows when requested
    if (hideExpired) filtered = filtered.filter((t) => !isExpired(t));

    const getCountryName = (t) =>
      mode === "export"
        ? t.destCountry?.name ?? ""
        : t.originCountry?.name ?? "";

    const expiryTime = (t) => {
      const expiryRaw = t.expiryDate ?? t.endDate ?? t.validUntil ?? null;
      if (!expiryRaw) return Infinity; // treat missing/invalid expiry as "far future" so it sorts as most recent
      const d = new Date(expiryRaw);
      if (isNaN(d)) return Infinity;
      return d.getTime();
    };

    // sort: by country, then active before expired, then by expiry (most recent first)
    const sorted = filtered.slice().sort((a, b) => {
      const aName = (getCountryName(a) || "").toLowerCase();
      const bName = (getCountryName(b) || "").toLowerCase();
      if (aName !== bName) return aName.localeCompare(bName);

      const aExp = isExpired(a);
      const bExp = isExpired(b);
      if (aExp !== bExp) return aExp ? 1 : -1; // active (not expired) first

      const aTime = expiryTime(a);
      const bTime = expiryTime(b);
      // both finite
      if (isFinite(aTime) && isFinite(bTime)) return bTime - aTime;
      // both infinite (no expiry) => keep stable
      if (!isFinite(aTime) && !isFinite(bTime)) return 0;
      // treat Infinity (no expiry) as most recent -> should come first
      if (!isFinite(aTime)) return -1;
      if (!isFinite(bTime)) return 1;
      return 0;
    });

    const count = sorted.length;
    const avgAd = (
      sorted.reduce((s, t) => s + (t.adValoremRate || 0), 0) /
      Math.max(1, count)
    ).toFixed(2);
    const avgSpec = (
      sorted.reduce((s, t) => s + (t.specificRate || 0), 0) / Math.max(1, count)
    ).toFixed(2);
    return (
      <div>
        <div className="mb-2 text-sm text-gray-300">
          Matches: {count} — Avg ad-valorem: {avgAd}% — Avg specific: {avgSpec}
        </div>
        <div className="overflow-auto">
          <table
            className="min-w-full text-sm"
            style={{ borderCollapse: "collapse" }}
          >
            <thead>
              <tr>
                <th className="text-left pr-4 pb-2">
                  {mode === "export" ? "Destination" : "Origin"}
                </th>
                <th className="text-left pr-4 pb-2">Effective</th>
                <th className="text-left pr-4 pb-2">Expiry</th>
                <th className="text-left pr-4 pb-2">Ad Valorem %</th>
                <th className="text-left pr-4 pb-2">Specific</th>
                <th className="text-left pr-4 pb-2">Products (HTS)</th>
                <th className="text-right pr-4 pb-2">Actions</th>
              </tr>
            </thead>
            <tbody>
              {sorted.map((t) => {
                const expiryRaw =
                  t.expiryDate ?? t.endDate ?? t.validUntil ?? null;
                const expired = isExpired(t);
                const rowStyle = {
                  borderTop: "1px solid rgba(255,255,255,0.05)",
                  ...(expired ? { backgroundColor: "rgba(255,0,0,0.06)" } : {}),
                };
                return (
                  <tr key={t.id} style={rowStyle}>
                    <td className="pr-4 py-2">
                      {mode === "export"
                        ? t.destCountry?.name ?? "-"
                        : t.originCountry?.name ?? "-"}
                    </td>
                    <td className="pr-4 py-2">{t.effectiveDate ?? "-"}</td>
                    <td className="pr-4 py-2">{expiryRaw ?? "-"}</td>
                    <td className="pr-4 py-2">{t.adValoremRate ?? "-"}</td>
                    <td className="pr-4 py-2">{t.specificRate ?? "-"}</td>
                    <td className="pr-4 py-2">
                      {(t.products || [])
                        .map((p) => p.htsCode ?? p.code ?? p.name)
                        .slice(0, 5)
                        .join(", ")}
                      {(t.products || []).length > 5 ? "…" : ""}
                    </td>
                    <td className="pr-4 py-2 text-right">
                      <button
                        className="mr-2 px-2 py-1 bg-blue-600 text-white rounded text-xs"
                        onClick={() => openEditForm(t)}
                      >
                        Edit
                      </button>
                      <button
                        className="px-2 py-1 bg-red-600 text-white rounded text-xs"
                        onClick={() => deleteTariff(t)}
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    );
  }

  // prepare dropdown options for countries and products
  const destinationNames = Array.from(
    new Set(tariffs.map((t) => t.destCountry?.name).filter(Boolean))
  ).sort();

  const productCodesFromTariffs = Array.from(
    new Set(
      tariffs
        .flatMap((t) => (t.products || []).map((p) => p.htsCode ?? p.code ?? p.name))
        .filter(Boolean)
    )
  ).sort();

  // ensure products selected in the form are included in options (so edit shows values even if not present globally)
  const productOptions = Array.from(
    new Set([...(productCodesFromTariffs || []), ...(formState.productsSelected || [])])
  ).sort();

  return (
    <div
      style={{
        backgroundColor: colors.background,
        color: colors.text,
        minHeight: "100vh",
      }}
    >
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
                onChange={(e) => setSelectedOrigin(e.target.value)}
              >
                {origins.map((o) => (
                  <option key={o} value={o}>
                    {o}
                  </option>
                ))}
              </select>

              <div className="ml-4 flex items-center space-x-2">
                <button
                  className={`px-3 py-1 rounded ${
                    mode === "export"
                      ? "bg-blue-600 text-white"
                      : "bg-gray-800 text-gray-300"
                  }`}
                  onClick={() => setMode("export")}
                >
                  Exporting from
                </button>
                <button
                  className={`px-3 py-1 rounded ${
                    mode === "import"
                      ? "bg-blue-600 text-white"
                      : "bg-gray-800 text-gray-300"
                  }`}
                  onClick={() => setMode("import")}
                >
                  Importing to
                </button>
                <label className="inline-flex items-center text-sm ml-3">
                  <input
                    type="checkbox"
                    className="form-checkbox mr-2"
                    checked={hideExpired}
                    onChange={(e) => setHideExpired(e.target.checked)}
                  />
                  Hide expired
                </label>
              </div>

              <div className="ml-auto flex items-center">
                <button
                  className="mr-2 px-3 py-1 bg-indigo-600 text-white rounded"
                  onClick={() => openCreateForm()}
                >
                  New tariff
                </button>

                <button
                  className="px-3 py-1 bg-green-600 text-white rounded"
                  onClick={() => exportFilteredCSV()}
                  disabled={!selectedOrigin || loading}
                >
                  Export CSV
                </button>
                <button
                  className="ml-2 px-3 py-1 bg-yellow-500 text-black rounded"
                  onClick={() => refreshCacheAndFetch()}
                  disabled={loading}
                  title="Clear cached tariffs (cookies/localStorage) and re-fetch from server"
                >
                  Refresh cache
                </button>
              </div>
            </div>

            {showForm && (
              <div className="mb-6 p-4 border rounded bg-black/30">
                <form onSubmit={submitForm}>
                  <div className="grid grid-cols-2 gap-3">
                    <div>
                      <label className="block text-sm">Origin</label>
                      <select
                        className="w-full px-2 py-1 bg-black border rounded"
                        value={formState.origin}
                        onChange={(e) =>
                          setFormState((s) => ({ ...s, origin: e.target.value }))
                        }
                      >
                        <option value="">-- select origin --</option>
                        {origins.map((o) => (
                          <option key={o} value={o}>
                            {o}
                          </option>
                        ))}
                      </select>
                    </div>
                    <div>
                      <label className="block text-sm">Destination</label>
                      <select
                        className="w-full px-2 py-1 bg-black border rounded"
                        value={formState.dest}
                        onChange={(e) =>
                          setFormState((s) => ({ ...s, dest: e.target.value }))
                        }
                      >
                        <option value="">-- select destination --</option>
                        {destinationNames.map((d) => (
                          <option key={d} value={d}>
                            {d}
                          </option>
                        ))}
                      </select>
                    </div>
                    <div>
                      <label className="block text-sm">Effective</label>
                      <input
                        className="w-full px-2 py-1 bg-black border rounded"
                        value={formState.effectiveDate}
                        onChange={(e) =>
                          setFormState((s) => ({ ...s, effectiveDate: e.target.value }))
                        }
                        placeholder="YYYY-MM-DD"
                      />
                    </div>
                    <div>
                      <label className="block text-sm">Expiry</label>
                      <input
                        className="w-full px-2 py-1 bg-black border rounded"
                        value={formState.expiryDate}
                        onChange={(e) =>
                          setFormState((s) => ({ ...s, expiryDate: e.target.value }))
                        }
                        placeholder="YYYY-MM-DD"
                      />
                    </div>
                    <div>
                      <label className="block text-sm">Ad valorem %</label>
                      <input
                        className="w-full px-2 py-1 bg-black border rounded"
                        value={formState.adValoremRate}
                        onChange={(e) =>
                          setFormState((s) => ({ ...s, adValoremRate: e.target.value }))
                        }
                        type="number"
                        step="0.01"
                      />
                    </div>
                    <div>
                      <label className="block text-sm">Specific</label>
                      <input
                        className="w-full px-2 py-1 bg-black border rounded"
                        value={formState.specificRate}
                        onChange={(e) =>
                          setFormState((s) => ({ ...s, specificRate: e.target.value }))
                        }
                        type="number"
                        step="0.01"
                      />
                    </div>
                    <div className="col-span-2">
                      <label className="block text-sm">
                        Products (select one or more)
                      </label>
                      <div className="flex gap-2">
                        <select
                          multiple
                          size={Math.min(10, Math.max(3, productOptions.length))}
                          className="w-full px-2 py-1 bg-black border rounded"
                          value={formState.productsSelected}
                          onChange={(e) => {
                            const selected = Array.from(e.target.selectedOptions).map(
                              (o) => o.value
                            );
                            setFormState((s) => ({ ...s, productsSelected: selected, productsRaw: selected.join(", ") }));
                          }}
                        >
                          {productOptions.map((p) => (
                            <option key={p} value={p}>
                              {p}
                            </option>
                          ))}
                        </select>
                      </div>
                      <div className="text-xs text-gray-400 mt-1">
                        Tip: hold Cmd/Ctrl (or Shift) to select multiple.
                      </div>
                    </div>
                  </div>

                  {formError && (
                    <div className="text-red-400 mt-2">{formError}</div>
                  )}

                  <div className="mt-3 flex items-center">
                    <button
                      type="submit"
                      className="px-3 py-1 bg-green-600 text-white rounded"
                      disabled={isSaving}
                    >
                      {isSaving ? "Saving..." : editingTariff ? "Save" : "Create"}
                    </button>
                    <button
                      type="button"
                      className="ml-2 px-3 py-1 bg-gray-700 text-white rounded"
                      onClick={() => closeForm()}
                      disabled={isSaving}
                    >
                      Cancel
                    </button>
                  </div>
                </form>
              </div>
            )}

            <FilteredTable
              tariffs={tariffs}
              origin={selectedOrigin}
              mode={mode}
            />
          </div>
        )}
      </div>
    </div>
  );
}
