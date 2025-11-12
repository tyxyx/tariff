"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Calculator, Settings, Users, User, TextSearch, Map , MonitorCog, Monitor, Server } from "lucide-react";
import { colors } from "@/styles/colors";
import PageHeader from "@/components/ui/PageHeader";
export default function DashboardPage() {
  const [currentUserEmail, setCurrentUserEmail] = useState("");
  const [currentUserRole, setCurrentUserRole] = useState("");

  useEffect(() => {
    let mounted = true;
    const fetchMe = async () => {
      try {
        const meRes = await fetch('/api/users/me');
        if (!meRes.ok) return;
        const meData = await meRes.json();
        if (!mounted) return;
        setCurrentUserEmail(meData.username);
        setCurrentUserRole(meData.role);
      } catch (e) {
        // optionally handle error
      }
    };
    fetchMe();
    return () => { mounted = false };
  }, []);

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

          {/* Tariff Simulator (document-based predictions) */}
          <Card className="hover:shadow-lg transition-shadow cursor-pointer">
            <CardHeader>
              <MonitorCog className="h-12 w-12 text-primary mb-4" />
              <CardTitle>Tariff Simulator</CardTitle>
              <CardDescription>
                Upload policy or news PDFs to generate a country-focused tariff
                impact report
              </CardDescription>
            </CardHeader>
            <CardContent>
              <Link href="/simulation">
                <Button className="w-full">Open Simulator</Button>
              </Link>
            </CardContent>
          </Card>

          <Card className="hover:shadow-lg transition-shadow cursor-pointer">
            <CardHeader>
              <TextSearch className="h-12 w-12 text-primary mb-4" />
              <CardTitle>Data</CardTitle>
              <CardDescription>
                Browse current tariff schedules and tables
              </CardDescription>
            </CardHeader>
            <CardContent>
              <Link href="/data">
                <Button className="w-full">Open Data</Button>
              </Link>
            </CardContent>
          </Card>

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
          {currentUserRole === 'admin' && (
            <Card className="hover:shadow-lg transition-shadow cursor-pointer">
              <CardHeader>
                <Server className="h-12 w-12 text-primary mb-4" />
                <CardTitle>Admin Panel</CardTitle>
                <CardDescription>
                  Manage users and administrative settings
                </CardDescription>
              </CardHeader>
              <CardContent>
                <Link href="/admin">
                  <Button className="w-full">Go to Admin</Button>
                </Link>
              </CardContent>
            </Card>
          )}
        </div>
      </div>
    </div>
  );
}
