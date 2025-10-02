import Link from "next/link";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Calculator, Settings, Users, User, History, Map } from "lucide-react";
import { colors } from "@/styles/colors";
import PageHeader from "@/components/ui/PageHeader";
export default function DashboardPage() {
  

  return (
    <div
      className="min-h-screen"
      style={{ backgroundColor: colors.background, color: colors.text }}
    >
      {/* Header */}
      <PageHeader />

      <div className="container mx-auto px-4 py-8">
        <div className="mb-8">
          <h2 className="text-3xl font-bold text-foreground mb-2">Dashboard</h2>
          <p className="text-muted-foreground">
            Access your tariff calculation tools and manage your account
          </p>
        </div>

        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
          {/* Tariff Calculator */}
          <Card className="hover:shadow-lg transition-shadow cursor-pointer">
            <CardHeader>
              <Calculator className="h-12 w-12 text-primary mb-4" />
              <CardTitle>Tariff Calculator</CardTitle>
              <CardDescription>
                Calculate import duties and taxes for products between countries
              </CardDescription>
            </CardHeader>
            <CardContent>
              <Link href="/calculator">
                <Button className="w-full">Start Calculating</Button>
              </Link>
            </CardContent>
          </Card>

          {/* Calculation History */}
          {/* <Card className="hover:shadow-lg transition-shadow cursor-pointer">
            <CardHeader>
              <History className="h-12 w-12 text-primary mb-4" />
              <CardTitle>Calculation History</CardTitle>
              <CardDescription>
                View your calculation history and saved results
              </CardDescription>
            </CardHeader>
            <CardContent>
              <Link href="/history">
                <Button variant="outline" className="w-full bg-transparent">
                  View History
                </Button>
              </Link>
            </CardContent>
          </Card> */}

          {/* Tariff Heatmap */}
          {/* <Card className="hover:shadow-lg transition-shadow cursor-pointer">
            <CardHeader>
              <Map className="h-12 w-12 text-primary mb-4" />
              <CardTitle>Tariff Heatmap</CardTitle>
              <CardDescription>
                Visualize tariff rates across countries with interactive heatmap
              </CardDescription>
            </CardHeader>
            <CardContent>
              <Link href="/heatmap">
                <Button variant="outline" className="w-full bg-transparent">
                  View Heatmap
                </Button>
              </Link>
            </CardContent>
          </Card> */}

          {/* Admin Panel - Only show for admin users */}
          {/* {user.role === "admin" && (
            <Card className="hover:shadow-lg transition-shadow cursor-pointer">
              <CardHeader>
                <Users className="h-12 w-12 text-primary mb-4" />
                <CardTitle>Admin Panel</CardTitle>
                <CardDescription>
                  Manage tariff data, users, and system settings
                </CardDescription>
              </CardHeader>
              <CardContent>
                <Link href="/admin">
                  <Button className="w-full">Open Admin Panel</Button>
                </Link>
              </CardContent>
            </Card>
          )} */}

          {/* Profile Settings */}
          <Card>
            <CardHeader>
              <Settings className="h-12 w-12 text-primary mb-4" />
              <CardTitle>Profile Settings</CardTitle>
              <CardDescription>
                Update your account information and preferences
              </CardDescription>
            </CardHeader>
            <CardContent>
              <Link href="/profile">
                <Button variant="outline" className="w-full bg-transparent">
                  Manage Profile
                </Button>
              </Link>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
