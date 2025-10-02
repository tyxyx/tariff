import React, { useState } from "react";
import { colors } from "@/styles/colors";

export function Input({ label, type = "text", id, value, onChange }) {
  const [focused, setFocused] = useState(false);

  const isActive = focused || (value && value.length > 0);

  const wrapperStyle = {
    position: "relative",
  };

  const inputStyle = {
    padding: "8px 16px",
    border: "1px solid #fff",
    borderRadius: 6,
    width: "100%",
  };

  const labelStyle = {
    position: "absolute",
    left: isActive ? 8 : 16,
    top: isActive ? -5 : 10,
    padding: "0 4px",
    backgroundColor: colors.buttonDark,
    color: colors.cardMuted,
    fontSize: isActive ? 14 : 16,
    lineHeight: 1,
    transition: "all 0.15s ease-out",
    pointerEvents: "none",
  };

  return (
    <div style={wrapperStyle}>
      <input
        id={id}
        type={type}
        style={inputStyle}
        value={value}
        onFocus={() => setFocused(true)}
        onBlur={() => setFocused(false)}
        onChange={onChange}
      />
      <label htmlFor={id} style={labelStyle}>
        {label}
      </label>
    </div>
  );
}
