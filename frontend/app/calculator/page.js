import Link from "next/link";
import { Button } from "@/components/ui/button";
import { colors } from "@/styles/colors";
import { Card, CardContent } from "@/components/ui/card";
import { Calculator } from "lucide-react";
import Header from "@/components/ui/header";

export default function CalculatorPage() {
  return (
    <div
      className="min-h-screen"
      style={{ backgroundColor: colors.background, color: colors.text }}
    >
      {/* Header */}
      <Header/>
    </div>
  );
}