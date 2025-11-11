"use client";
import { useState } from "react";
import PageHeader from "@/components/ui/PageHeader";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { colors } from "@/styles/colors";

const countries = [
  "United States",
  "China",
  "Germany",
  "Japan",
  "Singapore",
  "India",
  "Australia",
  "United Kingdom",
];

export default function SimulationPage() {
  const [country, setCountry] = useState("China");
  const [pdfFile, setPdfFile] = useState(null);
  const [running, setRunning] = useState(false);
  const [report, setReport] = useState("");
  const [error, setError] = useState("");

  async function handleGenerate() {
    if (!pdfFile) {
      setError("Please upload a PDF to generate a report.");
      return;
    }
    setRunning(true);
    setReport("");
    setError("");
    try {
      const formData = new FormData();
      formData.append("file", pdfFile);
      formData.append("country", country);

      const res = await fetch(`http://${process.env.NEXT_PUBLIC_BACKEND_EC2_HOST}:8080/api/predict`, {
        method: "POST",
        body: formData,
      });
      if (!res.ok) throw new Error("Generation failed");
      const text = await res.text();
      setReport(text);
    } catch (e) {
      setError("Failed to generate report. Please try again.");
    } finally {
      setRunning(false);
    }
  }

  return (
    <div style={{ backgroundColor: colors.background, color: colors.text, minHeight: "100vh" }}>
      <PageHeader />

      <div className="container mx-auto px-6 py-10">
        <div className="flex flex-col lg:flex-row gap-8">
          <div className="lg:w-1/3">
            <Card>
              <CardHeader>
                <CardTitle>Tariff Simulator</CardTitle>
                <CardDescription>Upload a PDF and generate a report</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div>
                    <label className="block mb-1 font-medium">Country</label>
                    <select
                      className="w-full border rounded px-3 py-2"
                      value={country}
                      onChange={e => setCountry(e.target.value)}
                      style={{ backgroundColor: "black", color: "white" }}
                    >
                      {countries.map(c => (
                        <option key={c} value={c}>{c}</option>
                      ))}
                    </select>
                  </div>

                  <div>
                    <label className="block mb-1 font-medium">Upload Report (PDF)</label>
                    <input
                      type="file"
                      accept="application/pdf"
                      onChange={e => setPdfFile(e.target.files?.[0] ?? null)}
                      className="block w-full text-sm text-gray-400 file:mr-4 file:py-2 file:px-4 file:rounded file:border-0 file:text-sm file:font-semibold file:bg-gray-700 file:text-white hover:file:bg-gray-600"
                    />
                  </div>

                  <div>
                    <Button className="w-full" onClick={handleGenerate} disabled={running}>
                      {running ? "Generating..." : "Generate Report"}
                    </Button>
                  </div>

                  {error && <div className="text-red-400 text-sm">{error}</div>}
                </div>
              </CardContent>
            </Card>
          </div>

          <div className="lg:w-2/3">
            <div className="rounded-lg p-8" style={{ backgroundColor: "#0b0b0b" }}>
              <h1 className="text-3xl font-extrabold text-center mb-6">The Expected Impact of Tariffs in {country}</h1>

              {report ? (
                <div className="prose prose-invert max-w-none whitespace-pre-line text-sm" style={{ lineHeight: 1.6 }}>
                  {report}
                </div>
              ) : (
                <div className="text-center text-gray-400">
                  <p className="mb-4">No report yet. Upload a PDF and click &quot;Generate Report&quot; to see predictions and a simulated impact analysis.</p>
                  <p className="text-sm">Tip: Use news articles or policy documents that mention tariff changes for best results.</p>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
