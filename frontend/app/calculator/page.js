"use client";
import { Button } from "@/components/ui/button";
import { colors } from "@/styles/colors";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Calculator } from "lucide-react";
import { useState, useEffect } from "react";
import PageHeader from "@/components/ui/PageHeader";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";
import "react-datepicker/dist/react-datepicker.css";
import "./datepicker-dark.css";

export default function CalculatorPage() {
  const [countries, setCountries] = useState([]);
  const [products, setProducts] = useState([]);
  const [product, setProduct] = useState("");
  const [originCountry, setImportCountry] = useState("");
  const [destCountry, setExportCountry] = useState("");
  const [adValoremRate, setAdValoremRate] = useState(0);
  const [specificRate, setSpecificRate] = useState(0);
  const [quantity, setQuantity] = useState(1);
  const [unitPrice, setUnitPrice] = useState(1000);
  const [calculationDate, setCalculationDate] = useState("");

  // Fetch countries and products on page load
  useEffect(() => {
    fetch(`http://${process.env.NEXT_PUBLIC_BACKEND_EC2_HOST}:8080/api/countries`)
      .then((res) => res.json())
      .then((data) => setCountries(data))
      .catch(() => setCountries([]));

    fetch(`http://${process.env.NEXT_PUBLIC_BACKEND_EC2_HOST}:8080/api/products`)
      .then((res) => res.json())
      .then(data => {
        console.log('Fetched products:', data);
        // Make sure data is an array
        if (Array.isArray(data)) setProducts(data);
        else setProducts([]);
      })
      .catch(() => setProducts([]));
  }, []);

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
          setAdValoremRate(data.adValoremRate ?? 0);
          setSpecificRate(data.specificRate ?? 0);
        })
        .catch(() => setTariffRate(0));
    } else {
      setAdValoremRate(0);
      setSpecificRate(0);
    }
  }, [product, originCountry, destCountry, calculationDate]);

  // Calculate tariff amount
  const amountSpecific = specificRate * quantity;
  const amountAdValorem = (adValoremRate / 100) * (unitPrice * quantity);


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
                      <option key={product.name} value={product.name}>{product.name}</option>
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
                      <option key={country.name} value={country.name}>{country.name}</option>
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
                      <option key={country.name} value={country.name}>{country.name}</option>
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
                  <label className="block mb-1 font-medium">Specific Rate</label>
                  <input
                    type="text"
                    id="specificRate"
                    className="w-full border rounded px-3 py-2"
                    value={specificRate}
                    readOnly
                    style={{ backgroundColor: "black", color: "white" }}
                  />
                </div>
                <div>
                  <label className="block mb-1 font-medium">Ad Valorem Rate (%)</label>
                  <input
                    type="text"
                    id="adValoremRate"
                    className="w-full border rounded px-3 py-2"
                    value={adValoremRate}
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
              <li><strong>Specific Rate:</strong> {specificRate}</li>
              <li><strong>Ad Valorem Rate (%):</strong> {adValoremRate}</li>
              <li><strong>Specific Duty Amount:</strong> {amountSpecific.toLocaleString()}</li>
              <li><strong>Ad Valorem Duty Amount:</strong> {amountAdValorem.toLocaleString()}</li>
            </ul>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
