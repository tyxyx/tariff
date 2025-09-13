import Link from "next/link";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { colors } from "@/styles/colors";

export default function HomePage() {
  return (
    <div
      className="min-h-screen"
      style={{ backgroundColor: colors.background, color: colors.text }}
    >
      {/* Header */}
      <header
        style={{ backgroundColor: colors.background, color: colors.text }}
      >
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <h1 className="text-2xl font-bold" style={{ color: colors.text }}>
                TariffCalc Pro
              </h1>
            </div>
            <div className="flex items-center gap-4">
              <Link href="/login">
                <Button>Login</Button>
              </Link>
              {/* <Link href="/register">
                <Button>Get Started</Button>
              </Link> */}
            </div>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <section
        className="py-20 px-4"
        style={{ backgroundColor: colors.card}}
      >
        <div className="container mx-auto max-w-4xl text-center">
          <h2
            className="text-4xl md:text-6xl font-bold mb-6 text-balance"
            style={{ color: colors.text }}
          >
            Calculate Import Tariffs with Precision
          </h2>
          <p
            className="text-xl mb-8 max-w-2xl mx-auto"
            style={{ color: colors.mutedText }}
          >
            Professional tariff calculation platform for international trade.
            Get accurate duty rates, manage product classifications, and
            streamline your import/export operations.(edit ltr)
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Link href="/login">
              <Button size="lg" className="w-full sm:w-auto">
                Start Calculating
              </Button>
            </Link>
          </div>
        </div>
      </section>

      {/* Features */}
      <section className="py-16 px-4" style={{ backgroundColor: colors.background }}>
        <div className="container mx-auto max-w-6xl">
          <h3
            className="text-3xl font-bold text-center mb-12"
            style={{ color: colors.text }}
          >
            Why Choose TariffCalc Pro?(edit as we implement more features)
          </h3>
          <div className="grid md:grid-cols-3 gap-8">
            <Card>
              <CardHeader>
                <CardTitle>Accurate Calculations</CardTitle>
                <CardDescription>
                  Real-time tariff rates with comprehensive product
                  classification database
                </CardDescription>
              </CardHeader>
            </Card>
            <Card>
              <CardHeader>
                <CardTitle>Secure & Reliable</CardTitle>
                <CardDescription>
                  Enterprise-grade security with role-based access control and
                  audit trails
                </CardDescription>
              </CardHeader>
            </Card>
            <Card>
              <CardHeader>
                <CardTitle>Team Management</CardTitle>
                <CardDescription>
                  Multi-user support with admin controls for managing tariff
                  data and users
                </CardDescription>
              </CardHeader>
            </Card>
          </div>
        </div>
      </section>
    </div>
  );
}