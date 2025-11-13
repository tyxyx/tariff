"use client"; // 1. Needs to be a Client Component to use state

import { useState } from "react"; // 2. Import useState
import {
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
} from "@/components/ui/card";
import { UserTable } from "@/components/user-table";
import { colors } from "@/styles/colors";
import { Button } from "@/components/ui/button";
import PageHeader from "@/components/ui/PageHeader";
import { SignupForm } from "@/components/signup-form"; // 3. Import the SignupForm

export default function UserDashboard() {
  const [showAddUserForm, setShowAddUserForm] = useState(false);

  const handleUserAdded = () => {
    setShowAddUserForm(false); // Close the form
    // Refresh the UserTable. GAHH NO TIME LIAO
    window.location.reload();
  };

  return (
    <div
      className="min-h-screen"
      style={{ backgroundColor: colors.background, color: colors.text }}
    >
      <PageHeader />
      <main className="container mx-auto px-4 py-8">
        <Card>
          <CardHeader>
            <div className="flex justify-between items-center">
              <div>
                <CardTitle>User Management</CardTitle>
                <CardDescription>
                  Manage registered users and their credentials.
                </CardDescription>
              </div>

              <Button onClick={() => setShowAddUserForm(true)}>
                Add New User
              </Button>
            </div>
          </CardHeader>
          <CardContent>
            {showAddUserForm ? (
              <div
                className="mb-6 p-4 border rounded-md"
                style={{ borderColor: colors.text }}
              >
                <div className="flex justify-between items-center mb-4">
                  <h3 className="text-lg font-semibold">
                    Create a New User Account
                  </h3>
                  <Button
                    variant="ghost"
                    onClick={() => setShowAddUserForm(false)} // This is the new "Cancel"
                  >
                    Cancel
                  </Button>
                </div>

                <SignupForm isAdmin={true} onSuccess={handleUserAdded} />
              </div>
            ) : (
              <UserTable />
            )}
          </CardContent>
        </Card>
      </main>
    </div>
  );
}
