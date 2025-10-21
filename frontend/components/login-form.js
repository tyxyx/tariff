"use client"; // This is a client component

import React, { Children } from "react";
import { Button } from "@/components/ui/button";
import { useState } from "react";
import { useRouter } from "next/navigation";
import Cookies from "js-cookie";


export function LoginForm({ children, className = "" }) {
  const router = useRouter();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [isLoading, setIsLoading] = useState(false);

 const handleSubmit = async (e) => {
   e.preventDefault();
   setError("");
   setSuccess("");

   if (!email || !password) {
     setError("Email and password are required");
     return;
   }

   setIsLoading(true);

   try {
     const response = await fetch(
       `${process.env.NEXT_PUBLIC_API_URL}/api/users/login`,
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
       return;
     }

     setSuccess(data.message || "Login successful!");
     localStorage.setItem("userEmail", email);

     if (data.token) {
       Cookies.set("auth_token", data.token, {
         expires: 7,
         secure: true,
         sameSite: "Strict",
       });
     }

     router.push("/dashboard");
   } catch (err) {
     console.error("Login error:", err);
     setError("Network error. Please try again.");
   } finally {
     setIsLoading(false);
   }
 }; // ✅ All brackets are closed properly


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

        <Button className="mt-6" type="submit" disabled={isLoading}>
          {isLoading ? "Logging in..." : "Login"}
        </Button>
      </form>
    </div>
  );
}

export function LoginInput({
  label,
  value,
  onChange,
  type = "text",
  className = "",
}) {
  return (
    <div className={` flex flex-col ${className}`}>
      <label className="mb-2">{label}</label>
      <input
        className="p-3 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:outline-none"
        type={type}
        value={value}
        onChange={(e) => onChange(e.target.value)}
      />
    </div>
  );
}
