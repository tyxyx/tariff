"use client";

import React, { useState } from "react";
import { colors } from "@/styles/colors";
export function Button({
  children,
  onClick,
  type = "button",
  style = {},
  className = "",
}) {
  const [isHovered, setIsHovered] = useState(false);
  return (
    <button
      type={type}
      onClick={onClick}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
      style={{
        backgroundColor: isHovered ? colors.buttonHover : colors.buttonDark,
        color: colors.buttonText,
      }}
      className={`px-4 py-2 rounded ${className}`}
    >
      {children}
    </button>
  );
}
