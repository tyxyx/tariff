// /components/ChangePasswordDialog.jsx
"use client";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardHeader, CardTitle } from "@/components/ui/card";
import React, { useState } from "react";

// Assuming you have a Dialog component for the pop-out,
// I'll define a basic wrapper here for context.
// REPLACE THIS WITH YOUR ACTUAL DIALOG/MODAL COMPONENT IF YOU HAVE ONE
const Dialog = ({ isOpen, onClose, children, title }) => {
  if (!isOpen) return null;
  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center">
      <div
        className="bg-white p-6 rounded-lg shadow-2xl w-full max-w-md"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-xl font-bold text-gray-800">{title}</h2>
          <Button
            onClick={onClose}
            variant="ghost"
            className="text-gray-500 hover:text-gray-900"
          >
            &times;
          </Button>
        </div>
        {children}
      </div>
    </div>
  );
};
// END OF DIALOG PLACEHOLDER

/**
 * Component to handle password updates for a specific user, rendered inside a dialog.
 * @param {object} props
 * @param {string} props.email The email (username) of the user to update.
 * @param {boolean} props.isOpen Controls the visibility of the dialog.
 * @param {function} props.onClose Function to call to close the dialog.
 */
export function ChangePasswordDialog({ email, isOpen, onClose }) {
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  // Note: We remove currentPassword input since this is being used by an admin
  // in the UserTable to update another user's password.
  // If the API requires the admin's password for verification, you would add that back.

  // Password validation function (kept the same)
  const isPasswordValid = (password) => {
    return (
      password.length >= 8 && /[A-Z]/.test(password) && /\d/.test(password)
    );
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    setIsLoading(true);

    if (newPassword !== confirmPassword) {
      setError("New password and confirmation do not match.");
      setIsLoading(false);
      return;
    }
    if (!isPasswordValid(newPassword)) {
      setError(
        "New password must be at least 8 characters, contain an uppercase letter and a number."
      );
      setIsLoading(false);
      return;
    }

    try {
      // NOTE: Adjusted URL to use process.env.NEXT_PUBLIC_API_URL and /api/users/change-password
      // If the backend requires a specific EC2 host/port, use the full URL from your original code.
      const apiUrl = `${process.env.NEXT_PUBLIC_API_URL}/api/users/change-password`;

      const response = await fetch(apiUrl, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          email, // Use the passed-in email for the target user
          // Removed 'password: currentPassword' from payload as per assumption
          newPassword,
        }),
      });

      const data = await response.json();

      if (!response.ok) {
        setError(data.message || "Failed to update password.");
        return;
      }

      setSuccess("Password updated successfully! Closing in 3 seconds...");
      setNewPassword("");
      setConfirmPassword("");

      // Auto-close after success
      setTimeout(() => {
        onClose();
      }, 3000);
    } catch (err) {
      setError("An error occurred. Please try again.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Dialog
      isOpen={isOpen}
      onClose={onClose}
      title={`Update Password for: ${email}`}
    >
      <Card className="shadow-none border-0">
        <form onSubmit={handleSubmit}>
          <div style={{ marginBottom: 18 }}>
            <p style={{ padding: "8px 0" }}>
              Password strength: Must be at least 8 characters long, contain an
              uppercase letter and a number.
            </p>

            {/* Input fields only for new and confirm password */}
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

            {error && <p className="text-red-500 mb-3">{error}</p>}
            {success && <p className="text-green-500 mb-3">{success}</p>}
          </div>

          <Button
            type="submit"
            disabled={
              isLoading ||
              !newPassword ||
              !confirmPassword ||
              !isPasswordValid(newPassword) ||
              newPassword !== confirmPassword
            }
          >
            {isLoading ? "Updating..." : "Update Password"}
          </Button>
        </form>
      </Card>
    </Dialog>
  );
}
