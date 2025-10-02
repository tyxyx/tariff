"use client";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { Calculator, User } from "lucide-react";
import { colors } from "@/styles/colors";
import { useRouter } from "next/navigation";
import { useEffect } from "react";

export default function PageHeader() {
  const router = useRouter();
  useEffect(() => {
    const email = localStorage.getItem("userEmail");
    if (!email) {
      router.push("/login");
    }
  }, [router]);
  const handleLogout = () => {
    localStorage.removeItem("userEmail");
    router.push("/login");
  };
  return (
    <header style={{ backgroundColor: colors.card, color: colors.text }}>
      <div className="container mx-auto px-4 py-4">
        <div className="flex items-center justify-between">
          <Link href="/">
            <div className="flex items-center gap-2">
              <Calculator className="h-8 w-8 text-primary" />
              <h1 className="text-2xl font-bold text-white">TariffCalc Pro</h1>
            </div>
          </Link>
          <div className="flex items-center gap-4">
            <span className="text-sm text-muted-foreground">
              {/* Welcome, {user.name} */}
              Welcome, User
            </span>
            <Link href="/profile">
              <Button
                variant="ghost"
                size="sm"
                className="gap-2 flex items-center"
              >
                <User className="h-4 w-4" />
                Profile
              </Button>
            </Link>
            {/* <Button variant="outline" onClick={handleLogout}> */}
            <Button variant="outline" onClick={handleLogout}>
              Sign Out
            </Button>
          </div>
        </div>
      </div>
    </header>
  );
}
