"use client"; // This is a client component

import React, { Children } from "react";
import Form from "next/form";
import { Button } from "@/components/ui/button";
import { useState } from "react";
import { useRouter } from "next/navigation";

export function LoginForm({ children, className = "" }) {
  const router = useRouter();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault(); // Prevent default form reload
    setError("");
    setSuccess("");

    try {
      const response = await fetch(
        `http://18.139.89.63:8080/api/users/login`,
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
        setError(data.message || "Login failed");
      }

      if (response.ok) {
        setSuccess(data.message || "Login successful!");
        router.push("/dashboard");
      }

      //   const data = await response.json();
      //   console.log("Login success:", data);

      // TODO: JWT token, redirect user, etc.
    } catch (err) {
      console.error(err.message);
    }
  };

  return (
    <div>
      <form onSubmit={handleSubmit} action="/dashboard">
        <div className={`space-y-6 ${className}`}>
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
        </div>

        {/* Messages */}
        {error && <p className="text-red-500">{error}</p>}
        {success && <p className="text-green-500">{success}</p>}

        <Button className="mt-6" type="submit">
          Login
        </Button>
      </form>
    </div>
  );
}

export function LoginInput({
  children,
  value,
  onChange,
  type = "text",
  className = "",
}) {
  return (
    <div className={` flex flex-col ${className}`}>
      <label className="mb-2">{children}</label>
      <input
        className="p-3 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:outline-none"
        type={type}
        value={value}
        onChange={(e) => onChange(e.target.value)}
      />
    </div>
  );
}
