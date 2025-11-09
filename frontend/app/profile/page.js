"use client";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardHeader, CardTitle } from "@/components/ui/card";
import { colors } from "@/styles/colors";
import React, { useState } from "react";
import PageHeader from "@/components/ui/PageHeader";
import Cookies from "js-cookie";

export default function DashboardPage() {
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  // Password validation function
  const isPasswordValid = (password) => {
    return (
      password.length >= 8 && /[A-Z]/.test(password) && /\d/.test(password)
    );
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");

    if (newPassword !== confirmPassword) {
      setError("New password and confirmation do not match.");
      return;
    }
    if (!isPasswordValid(newPassword)) {
      setError(
        "New password must be at least 8 characters, contain an uppercase letter and a number."
      );
      return;
    }

    // Get the token from the cookie
    const token = Cookies.get("authToken");
    if (!token) {
      setError("Authentication token not found.");
      return;
    }

    // extracct email from jwt headers instead of localStorage
    try {
      const decoded = jwtDecode(token);
      const email = decoded.email || decoded.sub;
      if (!email) {
        setError("Email not found in the token payload.");
        return;
      }
    } catch (error) {
      //todo fix ur user backend see why 403 error not appearing,
      // replace with backend error
      console.error("JWT Decode Error:", error);
      setError("Invalid authentication token.");
      return;
    }

    // // Changed to extract from jwt headers
    // const email = localStorage.getItem("userEmail");
    // if (!email) {
    //   setError("User not logged in.");
    //   return;
    // }

    try {
      const response = await fetch(
        `http://${process.env.NEXT_PUBLIC_BACKEND_EC2_HOST}:8080/api/users/change-password`,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`, // pass jwt shit
          },
          body: JSON.stringify({
            email,
            password: currentPassword,
            newPassword: newPassword,
          }),
        }
      );

      const data = await response.json();

      if (!response.ok) {
        setError(data.message || "Failed to update password.");
        return;
      }

      setSuccess("Password updated successfully!");
      setCurrentPassword("");
      setNewPassword("");
      setConfirmPassword("");
    } catch (err) {
      setError("An error occurred. Please try again.");
    }
  };

  return (
    <div
      className="min-h-screen"
      style={{ backgroundColor: colors.background, color: colors.text }}
    >
      {/* Header */}
      <PageHeader />

      <div className="container mx-auto px-4 py-8">
        <div className="mb-8">
          <h2 className="text-3xl font-bold text-foreground mb-2">Profile</h2>
          <p>Manage your account details</p>
        </div>

        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
          <Card className="hover:shadow-lg transition-shadow cursor-pointer">
            <CardHeader>
              <CardTitle>Change Password</CardTitle>

              <form onSubmit={handleSubmit}>
                <div style={{ marginBottom: 18 }}>
                  <p style={{ padding: "8px 0" }}>
                    Password strength: Password must be at least 8 characters
                    long, contain an uppercase letter and a number.
                  </p>
                  <div style={{ marginBottom: 12 }}>
                    <Input
                      label="Current Password"
                      type="password"
                      value={currentPassword}
                      onChange={(e) => setCurrentPassword(e.target.value)}
                    />
                  </div>
                  <div style={{ marginBottom: 12 }}>
                    <Input
                      label="New Password"
                      type="password"
                      value={newPassword}
                      onChange={(e) => setNewPassword(e.target.value)}
                    />
                  </div>
                  <div style={{ marginBottom: 12 }}>
                    <Input
                      label="Confirm Password"
                      type="password"
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                    />
                  </div>
                  {error && (
                    <p style={{ color: "red", marginBottom: 12 }}>{error}</p>
                  )}
                  {success && (
                    <p style={{ color: "green", marginBottom: 12 }}>
                      {success}
                    </p>
                  )}
                </div>
                <Button
                  type="submit"
                  disabled={
                    !currentPassword ||
                    !newPassword ||
                    !confirmPassword ||
                    !isPasswordValid(newPassword) ||
                    newPassword !== confirmPassword
                  }
                >
                  Confirm Changes
                </Button>
              </form>
            </CardHeader>
          </Card>
        </div>
      </div>
    </div>
  );
}
