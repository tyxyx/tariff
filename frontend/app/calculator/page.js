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
  const [importCountry, setImportCountry] = useState("");
  const [exportCountry, setExportCountry] = useState("");
  const [tariffRate, setTariffRate] = useState(0);
  const [quantity, setQuantity] = useState(1);
  const [unitPrice, setUnitPrice] = useState(1000);
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [dateError, setDateError] = useState("");

  function getTariffRate(product, importCountry, exportCountry) {
    // Replace ltrrrrrrrrrrrr
    if (product && importCountry && exportCountry) {
      if (importCountry === exportCountry) return 0;
      if (product === "Laptops" && importCountry === "United States" && exportCountry === "China") return 0.15;
      return 0.10; // fixed for now~
    }
    return 0;
  }

  useEffect(() => {
    setTariffRate(getTariffRate(product, importCountry, exportCountry));
  }, [product, importCountry, exportCountry]);

  // Date validation
  useEffect(() => {
    if (startDate && endDate && startDate > endDate) {
      setDateError("Start date cannot be later than end date.");
    } else {
      setDateError("");
    }
  }, [startDate, endDate]);

  // Calculate costs (anyhow now)
  const totalImportCost = quantity * unitPrice * (1 + tariffRate);
  const totalExportEarnings = quantity * unitPrice;

  return (
    <div
      className="min-h-screen"
      style={{ backgroundColor: colors.background, color: colors.text }}
    >
      <PageHeader />
      <div className="container mx-auto px-4 py-8 flex flex-col md:flex-row gap-8">
        {/* Calculator Card */}
        <Card className="max-w-lg w-full mx-auto">
          <CardHeader>
            <Calculator className="h-10 w-10 text-primary mb-2" />
            <CardTitle>Tariff Calculator</CardTitle>
            <CardDescription>
              Enter product details and select countries to calculate tariffs.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form className="space-y-4">
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
                  value={importCountry}
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
                  value={exportCountry}
                  onChange={e => setExportCountry(e.target.value)}
                >
                  <option value="" disabled hidden>Select country</option>
                  {countries.map(country => (
                    <option key={country} value={country}>{country}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block mb-1 font-medium">Start Date</label>
                <DatePicker
                  selected={startDate}
                  onChange={date => setStartDate(date)}
                  selectsStart
                  startDate={startDate}
                  endDate={endDate}
                  maxDate={endDate}
                  placeholderText="Select start date"
                  className="w-full border rounded px-3 py-2"
                  popperPlacement="bottom"
                  style={{ backgroundColor: "black", color: "white" }}
                />
              </div>
              <div>
                <label className="block mb-1 font-medium">End Date</label>
                <DatePicker
                  selected={endDate}
                  onChange={date => setEndDate(date)}
                  selectsEnd
                  startDate={startDate}
                  endDate={endDate}
                  minDate={startDate}
                  placeholderText="Select end date"
                  className="w-full border rounded px-3 py-2"
                  popperPlacement="bottom"
                  style={{ backgroundColor: "black", color: "white" }}
                />
              </div>
              {dateError && (
                <div className="text-red-500 text-sm">{dateError}</div>
              )}
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
          </CardContent>
        </Card>
        {/* Tariff Summary Card */}
        <Card className="max-w-md w-full mx-auto">
          <CardHeader>
            <CardTitle>Tariff Summary</CardTitle>
            <CardDescription>
              See your calculated costs and earnings.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <ul className="space-y-2">
              <li>
                <strong>Product:</strong> {product || "-"}
              </li>
              <li>
                <strong>Import Country:</strong> {importCountry || "-"}
              </li>
              <li>
                <strong>Export Country:</strong> {exportCountry || "-"}
              </li>
              <li>
                <strong>Quantity:</strong> {quantity}
              </li>
              <li>
                <strong>Unit Price:</strong> ${unitPrice}
              </li>
              <li>
                <strong>Start Date:</strong> {startDate ? startDate.toLocaleDateString() : "-"}
              </li>
              <li>
                <strong>End Date:</strong> {endDate ? endDate.toLocaleDateString() : "-"}
              </li>
              <li>
                <strong>Tariff Rate:</strong> {tariffRate ? `${(tariffRate * 100).toFixed(2)}%` : "-"}
              </li>
              <li>
                <strong>Total Import Cost:</strong>{" "}
                {product && importCountry && exportCountry && quantity && unitPrice
                  ? `$${totalImportCost.toLocaleString()}`
                  : "-"}
              </li>
              <li>
                <strong>Total Export Earnings:</strong>{" "}
                {product && importCountry && exportCountry && quantity && unitPrice
                  ? `$${totalExportEarnings.toLocaleString()}`
                  : "-"}
              </li>
            </ul>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}