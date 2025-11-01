"use client";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { colors } from "@/styles/colors";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Calculator } from "lucide-react";
import { useState } from "react";
import { useEffect } from "react";
import PageHeader from "@/components/ui/PageHeader";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";
import "react-datepicker/dist/react-datepicker.css";
import "./datepicker-dark.css";

// Hardcoded for now
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

const products = [
  "Semiconductors",
  "Laptops",
  "Smartphones",
  "Solid State Drives (SSD)",
  "Graphic Processing Units (GPU)"
];

export default function CalculatorPage() {
  const [product, setProduct] = useState("");
  const [originCountry, setImportCountry] = useState("");
  const [destCountry, setExportCountry] = useState("");
  const [tariffRate, setTariffRate] = useState(0);
  const [quantity, setQuantity] = useState(1);
  const [unitPrice, setUnitPrice] = useState(1000);
  const [calculationDate, setCalculationDate] = useState("");
  const [dateError, setDateError] = useState("");
  const [activeTab, setActiveTab] = useState("calculator");
  // Predict tab state
  const [pdfFile, setPdfFile] = useState(null);
  const [predicting, setPredicting] = useState(false);
  const [predictionResult, setPredictionResult] = useState("");
  const [predictionError, setPredictionError] = useState("");

  // Handler for Predict button
  async function handlePredict() {
    if (!pdfFile) return;
    setPredicting(true);
    setPredictionResult("");
    setPredictionError("");
    try {
      const formData = new FormData();
      formData.append("file", pdfFile);
      const res = await fetch(`http://${process.env.NEXT_PUBLIC_BACKEND_EC2_HOST}:8080/api/predict`, {
        method: "POST",
        body: formData,
      });
      if (!res.ok) throw new Error("Prediction failed");
      const text = await res.text();
      setPredictionResult(text);
    } catch (err) {
      setPredictionError("Prediction failed. Please try again.");
    } finally {
      setPredicting(false);
    }
  }

  // function getTariffRate(product, originCountry, destCountry) {
  //   // Replace ltrrrrrrrrrrrr
  //   if (product && originCountry && destCountry) {
  //     if (originCountry === destCountry) return 0;
  //     if (product === "Laptops" && originCountry === "United States" && destCountry === "China") return 0.15;
  //     return 0.10; // fixed for now~
  //   }
  //   return 0;
  // }

  // useEffect(() => {
  //   setTariffRate(getTariffRate(product, originCountry, destCountry));
  // }, [product, originCountry, destCountry]);

   useEffect(() => {
    // Only call API if all fields are filled and calculationDate is valid
    if (
      product &&
      originCountry &&
      destCountry &&
      calculationDate
    ) {
      // Format date as YYYY-MM-DD
      const formattedDate = calculationDate.toISOString().split("T")[0];
      // TODO: change this to process.env
      fetch(`http://${process.env.NEXT_PUBLIC_BACKEND_EC2_HOST}:8080/api/tariffs/particular-tariff-rate`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          date: formattedDate,
          originCountry,
          destCountry,
          productName: product,
        }),
      })
        .then(res => res.json())
        .then(data => {
          // Assume API returns { tariffRate: 0.15 }
          setTariffRate(data.rate ?? 0);
        })
        .catch(() => setTariffRate(0));
    } else {
      setTariffRate(0);
    }
  }, [product, originCountry, destCountry, calculationDate]);

  // Calculate tariff amount
  const tariffAmount = quantity * unitPrice * (tariffRate);

  return (
    <div
      className="min-h-screen"
      style={{ backgroundColor: colors.background, color: colors.text }}
    >
      <PageHeader />
      <div className="container mx-auto px-4 py-8 flex flex-col md:flex-row gap-8">
        {/* Calculator Card with Tabs */}
        <Card className="max-w-lg w-full mx-auto">
          <CardHeader>
            <Calculator className="h-10 w-10 text-primary mb-2" />
            <CardTitle>Tariff Calculator</CardTitle>
            <CardDescription>
              Enter product details and select countries to calculate tariffs.
            </CardDescription>
          </CardHeader>
          <CardContent>
            {/* Tabs */}
            <div className="mb-4 flex border-b border-gray-700">
              <button
                className={`px-4 py-2 focus:outline-none ${activeTab === "calculator" ? "border-b-2 border-blue-500 text-blue-500" : "text-gray-400"}`}
                onClick={() => setActiveTab("calculator")}
              >
                Calculator
              </button>
              <button
                className={`px-4 py-2 focus:outline-none ${activeTab === "predict" ? "border-b-2 border-blue-500 text-blue-500" : "text-gray-400"}`}
                onClick={() => setActiveTab("predict")}
              >
                Predict
              </button>
            </div>
            {/* Tab Content */}
            {activeTab === "calculator" && (
              <form className="space-y-4">
                {/* ...existing calculator form code... */}
                <div>
                  <label className="block mb-1 font-medium">Product Name</label>
                  <select
                    className="w-full border rounded px-3 py-2"
                    style={{ backgroundColor: "black", color: "white" }}
                    value={product}
                    onChange={e => setProduct(e.target.value)}
                  >
                    <option value="" disabled hidden>Select product</option>
                    {products.map(product => (
                      <option key={product} value={product}>{product}</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block mb-1 font-medium">Import Country</label>
                  <select
                    className="w-full border rounded px-3 py-2"
                    style={{ backgroundColor: "black", color: "white" }}
                    value={originCountry}
                    onChange={e => setImportCountry(e.target.value)}
                  >
                    <option value="" disabled hidden>Select country</option>
                    {countries.map(country => (
                      <option key={country} value={country}>{country}</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block mb-1 font-medium">Export Country</label>
                  <select
                    className="w-full border rounded px-3 py-2"
                    style={{ backgroundColor: "black", color: "white" }}
                    value={destCountry}
                    onChange={e => setExportCountry(e.target.value)}
                  >
                    <option value="" disabled hidden>Select country</option>
                    {countries.map(country => (
                      <option key={country} value={country}>{country}</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block mb-1 font-medium">Calculation Date</label>
                  <DatePicker
                    selected={calculationDate}
                    onChange={date => setCalculationDate(date)}
                    selectsStart
                    calculationDate={calculationDate}
                    placeholderText="Select calculation date"
                    className="w-full border rounded px-3 py-2"
                    popperPlacement="bottom"
                    style={{ backgroundColor: "black", color: "white" }}
                  />
                </div>
                <div>
                  <label className="block mb-1 font-medium">Quantity</label>
                  <input
                    type="number"
                    min={1}
                    className="w-full border rounded px-3 py-2"
                    style={{ backgroundColor: "black", color: "white" }}
                    value={quantity}
                    onChange={e => {
                      const val = e.target.value.replace(/^0+/, "");
                      setQuantity(val === "" ? "" : Number(val));
                    }}
                    placeholder="Enter quantity"
                  />
                </div>
                <div>
                  <label className="block mb-1 font-medium">Unit Price</label>
                  <input
                    type="number"
                    min={0}
                    className="w-full border rounded px-3 py-2"
                    style={{ backgroundColor: "black", color: "white" }}
                    value={unitPrice}
                    onChange={e => {
                      const val = e.target.value.replace(/^0+/, "");
                      setUnitPrice(val === "" ? "" : Number(val));
                    }}
                    placeholder="Enter unit price"
                  />
                </div>
                <div>
                  <label className="block mb-1 font-medium">Tariff Rate</label>
                  <input
                    type="text"
                    className="w-full border rounded px-3 py-2"
                    value={tariffRate ? `${(tariffRate * 100).toFixed(2)}%` : ""}
                    readOnly
                    style={{ backgroundColor: "black", color: "white" }}
                  />
                </div>
              </form>
            )}
            {activeTab === "predict" && (
              <div className="space-y-4">
                <label className="block mb-1 font-medium">Upload News Article (PDF)</label>
                <input
                  type="file"
                  accept="application/pdf"
                  className="block w-full text-sm text-gray-400 file:mr-4 file:py-2 file:px-4 file:rounded file:border-0 file:text-sm file:font-semibold file:bg-gray-700 file:text-white hover:file:bg-gray-600"
                  onChange={e => {
                    if (e.target.files && e.target.files[0]) setPdfFile(e.target.files[0]);
                  }}
                />
                <Button className="w-full" type="button" onClick={handlePredict} disabled={!pdfFile || predicting}>
                  {predicting ? "Predicting..." : "Predict Tariff Changes"}
                </Button>
                {predictionResult && (
                  <div className="mt-2 text-sm text-green-400 whitespace-pre-line">{predictionResult}</div>
                )}
                {predictionError && (
                  <div className="mt-2 text-sm text-red-400">{predictionError}</div>
                )}
              </div>
            )}
          </CardContent>
        </Card>
        {/* Tariff Summary Card */}
        <Card className="max-w-md w-full mx-auto">
          <CardHeader>
            <CardTitle>Tariff Summary</CardTitle>
            <CardDescription>
              See your calculated costs.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <ul className="space-y-2">
              <li>
                <strong>Product:</strong> {product || "-"}
              </li>
              <li>
                <strong>Import Country:</strong> {originCountry || "-"}
              </li>
              <li>
                <strong>Export Country:</strong> {destCountry || "-"}
              </li>
              <li>
                <strong>Quantity:</strong> {quantity}
              </li>
              <li>
                <strong>Unit Price:</strong> ${unitPrice}
              </li>
              <li>
                <strong>Calculation Date:</strong> {calculationDate ? calculationDate.toLocaleDateString() : "-"}
              </li>
              <li>
                <strong>Tariff Rate:</strong> {tariffRate ? `${(tariffRate * 100).toFixed(2)}%` : "-"}
              </li>
              <li>
                <strong>Tariff Amount:</strong>{" "}
                {product && originCountry && destCountry && quantity && unitPrice
                  ? `$${tariffAmount.toLocaleString()}`
                  : "-"}
              </li>
            </ul>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
