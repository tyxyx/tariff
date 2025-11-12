"use client";

import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/card";
import { UserTable  } from "@/components/user-table";
import { colors } from "@/styles/colors";
import PageHeader from "@/components/ui/header";

export default function UserDashboard() {
  return (
  <div
        className="min-h-screen"
        style={{ backgroundColor: colors.background, color: colors.text }}
      >
        <PageHeader />
        <main className="container mx-auto px-4 py-8">
          <Card>
                <CardHeader>
                  <CardTitle>User Management</CardTitle>
                  <CardDescription>Manage registered users and their credentials.</CardDescription>
                </CardHeader>
                <CardContent>
                  <UserTable />
                </CardContent>
              </Card>
        </main>
      </div>
    );
}
