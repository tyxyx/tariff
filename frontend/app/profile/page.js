"use client";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Card,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Calculator, User } from "lucide-react";
import { colors } from "@/styles/colors";
import React, { useState } from "react";
export default function DashboardPage() {
  // const [newPassword, setNewPassword] = useState("");
  // const [confirmPassword, setConfirmPassword] = useState("");
  // const [error, setError] = useState("");

  // const handleSubmit = (e) => {
  //   e.preventDefault();
  //   if (newPassword !== confirmPassword) {
  //     setError("New password and confirmation do not match.");
  //   } else {
  //     setError("");
  //   }
  // };
  return (
    <div
      className="min-h-screen"
      style={{ backgroundColor: colors.background, color: colors.text }}
    >
      {/* Header */}
      <header style={{ backgroundColor: colors.card, color: colors.text }}>
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Calculator className="h-8 w-8 text-primary" />
              <h1 className="text-2xl font-bold text-foreground">
                TariffCalc Pro
              </h1>
            </div>
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
              <Button variant="outline">Sign Out</Button>
            </div>
          </div>
        </div>
      </header>

      <div className="container mx-auto px-4 py-8">
        <div className="mb-8">
          <h2 className="text-3xl font-bold text-foreground mb-2">Profile</h2>
          <p>Manage your account details</p>
        </div>

        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
        
          <Card className="hover:shadow-lg transition-shadow cursor-pointer">
            <CardHeader>
              <CardTitle>Change Password</CardTitle>

              <form>
                <div style={{ marginBottom: 18 }}>
                  <Input label="New Password" type="password" />
                  <p style={{ padding: "8px 0" }}>
                    Password strength: Use at least 8 characters.
                  </p>

                  <Input label="Confirm Password" type="password" />
                </div>
                <Button>Confirm Changes</Button>
              </form>
            </CardHeader>
          </Card>
        </div>
      </div>
    </div>
  );
}
