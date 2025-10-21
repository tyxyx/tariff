"use client";

import React, { useState } from "react";
import { Button } from "@/components/ui/button";
import { useRouter } from "next/navigation";
import Cookies from "js-cookie";
import { LoginInput } from "@/components/login-form";

export function SignupForm() {
  const router = useRouter(); // ✅ Hook at the top level

  // ✅ State hooks at the top level
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");

    if (!email || !password || !confirmPassword) {
      setError("All fields are required");
      return;
    }
    if (password !== confirmPassword) {
      setError("Passwords do not match");
      return;
    }

    setIsLoading(true);

    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/api/users/register`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ email, password }),
        }
      );

      const data = await response.json();

      if (!response.ok) {
        setError(data.message || "Signup failed");
        return;
      }

      if (data.token) {
        Cookies.set("auth_token", data.token, {
          expires: 7,
          secure: true,
          sameSite: "Strict",
        });
      }

      setSuccess("Account created successfully!");
      localStorage.setItem("userEmail", email);
      router.push("/dashboard");
    } catch (err) {
      console.error("Signup error:", err);
      setError("Network error. Please try again.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <div className="space-y-6">
        <LoginInput
          label="Email"
          value={email}
          onChange={setEmail}
          type="email"
        />
        <LoginInput
          label="Password"
          value={password}
          onChange={setPassword}
          type="password"
        />
        <LoginInput
          label="Confirm Password"
          value={confirmPassword}
          onChange={setConfirmPassword}
          type="password"
        />
      </div>

      {error && <p className="text-red-500 mt-3">{error}</p>}
      {success && <p className="text-green-500 mt-3">{success}</p>}

      <Button className="mt-6 w-full" type="submit" disabled={isLoading}>
        {isLoading ? "Creating account..." : "Sign Up"}
      </Button>
    </form>
  );
}
