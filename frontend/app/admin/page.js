"use client";
import React, { useState, useRef, useEffect } from "react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Pencil, Trash2, Plus, Check, X } from "lucide-react";
import { colors } from "@/styles/colors";
import PageHeader from "@/components/ui/PageHeader";

// Hardcoded for now
const initialTariffData = [
  {
    product: "Semiconductor",
    originCountry: "China",
    destinationCountry: "United States",
    effectiveDate: "2025-01-01",
    expiryDate: "2026-01-01",
    adValoremRate: 7.5,
    specificRate: 0.10,
    minQuantity: 1000,
    maxQuantity: 50000
  },
  {
    product: "Laptop",
    originCountry: "Germany",
    destinationCountry: "Japan",
    effectiveDate: "2025-03-15",
    expiryDate: "2026-03-15",
    adValoremRate: 5.0,
    specificRate: 0.05,
    minQuantity: 500,
    maxQuantity: 20000
  },
  {
    product: "Graphic processing unit",
    originCountry: "India",
    destinationCountry: "Singapore",
    effectiveDate: "2025-05-01",
    expiryDate: "2026-05-01",
    adValoremRate: 10.0,
    specificRate: 0.20,
    minQuantity: 200,
    maxQuantity: 10000
  },
  {
    product: "Solid state drive",
    originCountry: "United Kingdom",
    destinationCountry: "Australia",
    effectiveDate: "2024-11-20",
    expiryDate: "2025-11-20",
    adValoremRate: 3.0,
    specificRate: 0.07,
    minQuantity: 300,
    maxQuantity: 8000
  },
  {
    product: "Laptop",
    originCountry: "Japan",
    destinationCountry: "Germany",
    effectiveDate: "2025-02-28",
    expiryDate: null,
    adValoremRate: 4.5,
    specificRate: 0.12,
    minQuantity: 700,
    maxQuantity: 15000
  }
];


export default function AdminPage() {

  const [tariffData, setTariffData] = useState(initialTariffData);
  const [editingIndex, setEditingIndex] = useState(null);
  const [tempRow, setTempRow] = useState({});
  const rowRefs = useRef([]);

  const numericFields = [
    "adValoremRate",
    "specificRate",
    "minQuantity",
    "maxQuantity",
  ];

  const dateFields = ["effectiveDate", "expiryDate"];

  // Regex for yyyy-mm-dd
  const dateRegex = /^\d{4}-\d{2}-\d{2}$/;

  // Add: appends a blank row (you can customize fields)
  const handleAdd = () => {
    const newTariff = {
      product: "",
      originCountry: "",
      destinationCountry: "",
      effectiveDate: "",
      expiryDate: "",
      adValoremRate: 0,
      specificRate: 0,
      minQuantity: 0,
      maxQuantity: 0
    };
    setTariffData([...tariffData, newTariff]);
  };

  // On entering edit mode, scroll to row
  useEffect(() => {
    if (editingIndex !== null && rowRefs.current[editingIndex]) {
      rowRefs.current[editingIndex].scrollIntoView({ behavior: "smooth", block: "center" });
    }
  }, [editingIndex]);

  // Edit
  const handleEdit = (index) => {
    setEditingIndex(index);
    setTempRow(tariffData[index]);
  };

  // Confirm save
  const handleSave = (index) => {
    // validation check for numeric fields
    for (const field of numericFields) {
      const value = tempRow[field];
      if (value === "" || isNaN(value)) {
        alert(`"${field}" must be a valid number.`);
        return;
      }
    }

    // Date validation for effectiveDate (required)
    const effVal = tempRow.effectiveDate;
    if (!dateRegex.test(effVal) || isNaN(Date.parse(effVal))) {
      alert(`"effectiveDate" must be in yyyy-mm-dd format and valid.`);
      return;
    }

    // Expiry date: allow blank, "N/A", or valid date
    const expVal = tempRow.expiryDate;
    if (
      expVal &&
      expVal !== "N/A" &&
      (!dateRegex.test(expVal) || isNaN(Date.parse(expVal)))
    ) {
      alert(`"expiryDate" must be N/A or a valid yyyy-mm-dd date.`);
      return;
    }

    const updatedData = [...tariffData];
    updatedData[index] = tempRow;
    setTariffData(updatedData);
    setEditingIndex(null);
  };

  // Cancel edit
  const handleCancel = () => {
    setEditingIndex(null);
    setTempRow({});
  };

  // Handle input changes
  const handleChange = (e, field) => {
    const value = e.target.value;

    // Restrict non-numeric input for numeric fields
    if (numericFields.includes(field)) {
      if (value === "" || /^[0-9]*\.?[0-9]*$/.test(value)) {
        setTempRow({ ...tempRow, [field]: value });
      }
    } else {
      setTempRow({ ...tempRow, [field]: value });
    }
  };

  // Delete row
  const handleDelete = (index) => {
    const updatedTariffs = tariffData.filter((_, i) => i !== index);
    setTariffData(updatedTariffs);
  };

  const cellStyle = {
    border: "1px solid",
    textAlign: "center",
    padding: "10px 4px" // 16px top/bottom, 8px left/right
  };

  return (
    <div
      className="min-h-screen"
      style={{ backgroundColor: colors.background, color: colors.text }}
    >
      {/* Header */}
      <PageHeader />

      <Card className="w-[90vw] max-w-[1500px] mx-auto my-8">
        <CardHeader>
          <CardTitle>Tariff List</CardTitle>
        </CardHeader>
        <CardContent>
          <div style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            marginTop: "10px",
            marginBottom: "15px"
          }}>
            {/* Placeholder filter dropdown */}
            <select
              className="w-full border rounded px-3 py-2"
              style={{ backgroundColor: colors.card, color: "white", maxWidth: "200px" }}
            // value={filterValue}
            // onChange={e => setFilterValue(e.target.value)}
            >
              <option value="">Filter (placeholder)</option>
              <option value="china">China</option>
              <option value="germany">Germany</option>
              {/* ... */}
            </select>

            <button
              style={{
                color: "white",
                border: "1px solid #ccc",
                borderRadius: "6px",
                padding: "8px 16px",
                marginLeft: "16px",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                cursor: "pointer"
              }}
              onClick={handleAdd}
              title="Add"
            >
              <Plus size={20} color="white" />
            </button>
          </div>

          <table style={{
            width: "100%",
            borderCollapse: "collapse"
          }}>
            <thead>
              <tr>
                <th style={cellStyle}>Product</th>
                <th style={cellStyle}>Origin</th>
                <th style={cellStyle}>Destination</th>
                <th style={cellStyle}>Effective</th>
                <th style={cellStyle}>Expiry</th>
                <th style={cellStyle}>Ad Valorem Rate (%)</th>
                <th style={cellStyle}>Specific Rate</th>
                <th style={cellStyle}>Min Qty</th>
                <th style={cellStyle}>Max Qty</th>
                <th style={cellStyle}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {tariffData.map((tariff, idx) => (
                <tr
                  key={idx}
                  ref={el => rowRefs.current[idx] = el}
                  style={editingIndex === idx ? {
                    ...cellStyle,
                    background: "#2E4053",
                    boxShadow: "0 0 0 2px #00ff99"
                  } : undefined}
                >
                  {Object.keys(tariff).map((field, fieldIdx) => (
                    <td style={cellStyle} key={fieldIdx}>
                      {editingIndex === idx ? (
                        <input
                          type={
                            numericFields.includes(field) ? "number" :
                              dateFields.includes(field) ? "text" : "text"
                          }
                          value={tempRow[field]}
                          onChange={(e) => handleChange(e, field)}
                          style={{
                            backgroundColor: colors.card,
                            color: colors.cardText,
                            border: `1px solid ${colors.cardBorder}`,
                            borderRadius: "4px",
                            textAlign: "center",
                            width: "90%"
                          }}
                          placeholder={
                            dateFields.includes(field) ? "yyyy-mm-dd" : undefined
                          }
                        />
                      ) : (
                        tariff[field] ?? "N/A"
                      )}
                    </td>
                  ))}
                  <td style={cellStyle}>
                    {editingIndex === idx ? (
                      <>
                        <button
                          style={{
                            background: "none",
                            border: "none",
                            padding: "0 8px",
                            cursor: "pointer"
                          }}
                          onClick={() => handleSave(idx)}
                          title="Save"
                        >
                          <Check size={20} color="#00ff99" />
                        </button>
                        <button
                          style={{
                            background: "none",
                            border: "none",
                            padding: "0 8px",
                            cursor: "pointer"
                          }}
                          onClick={handleCancel}
                          title="Cancel"
                        >
                          <X size={20} color="red" />
                        </button>
                      </>
                    ) : (
                      <>
                        <button
                          style={{
                            background: "none",
                            border: "none",
                            padding: "0 8px",
                            cursor: "pointer"
                          }}
                          onClick={() => handleEdit(idx)}
                          title="Edit"
                        >
                          <Pencil size={20} color="#fff" />
                        </button>
                        <button
                          style={{
                            background: "none",
                            border: "none",
                            padding: "0 8px",
                            cursor: "pointer"
                          }}
                          onClick={() => handleDelete(idx)}
                          title="Delete"
                        >
                          <Trash2 size={20} color="red" />
                        </button>
                      </>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </CardContent>
      </Card>

    </div>

  )
}
