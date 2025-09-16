import React from "react";
import { colors } from "@/styles/colors";

export function Card({ children, className = "" }) {
  return (
    <div
      className={`border rounded shadow p-4 ${className}`}
      style={{
        backgroundColor: colors.card,
        color: colors.cardText,
        borderColor: colors.cardBorder,
        borderWidth: "1px",
        borderStyle: "solid",
      }}
    >
      {children}
    </div>
  );
}

export function CardHeader({ children, className = "" }) {
  return <div className={`mb-4 ${className}`}>{children}</div>;
}

export function CardTitle({ children, className = "" }) {
  return (
    <h3 className={`text-lg font-bold mb-2 ${className}`} style={{ color: colors.cardText }}>
      {children}
    </h3>
  );
}

export function CardDescription({ children, className = "" }) {
  return (
    <p className={`mb-2 ${className}`} style={{ color: colors.cardMuted }}>
      {children}
    </p>
  );
}

export function CardContent({ children, className = "" }) {
  return <div className={`mb-4 ${className}`}>{children}</div>;
}