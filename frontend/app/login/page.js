import Link from "next/link";
import { Button } from "@/components/ui/button";
import { LoginForm, LoginInput } from "@/components/login-form";
import { colors } from "@/styles/colors";
import { Card, CardContent } from "@/components/ui/card";
import { Calculator } from "lucide-react";

export default function LoginPage() {
  return (
    <div
      className="min-h-screen flex items-center justify-center p-4"
      style={{ backgroundColor: colors.background, color: colors.text }}
    >
      <div className="w-full max-w-md">
        {/* Header */}
        <div className="text-center mb-8">
          <Link
            href="/"
            className="inline-flex items-center gap-2 text-2xl font-bold text-foreground"
          >
            <Calculator className="h-8 w-8 text-primary" />
            TariffCalc Pro
          </Link>
          <p className="text-muted-foreground mt-2">Sign in to your account</p>
        </div>
        <Card>
          <CardContent>
            <LoginForm>
              <LoginInput>Email</LoginInput>
              <LoginInput>Password</LoginInput>
            </LoginForm>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
