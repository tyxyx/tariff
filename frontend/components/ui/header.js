import Link from "next/link";
import { Button } from "@/components/ui/button";
import { Calculator } from "lucide-react";
import { colors } from "@/styles/colors";

export default function Header() {
  return (
    <header style={{ backgroundColor: colors.background, color: colors.text }}>
      <div className="container mx-auto px-4 py-4">
        <div className="flex items-center justify-between">
          <Link href="/">
            <div className="flex items-center gap-2">
              <Calculator className="h-8 w-8 text-primary" />
              <h1 className="text-2xl font-bold" style={{ color: colors.text }}>
                TariffCalc Pro
              </h1>
            </div>
          </Link>
          <div className="flex items-center gap-4">
            <Link href="/login">
              <Button>Login</Button>
            </Link>
            <Link href="/signup">
              <Button>Sign Up</Button>
            </Link>
          </div>
        </div>
      </div>
    </header>
  );
}
