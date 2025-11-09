import { useState, useEffect } from "react";
import { apiFetch } from "./apiClient"; // Adjust path if necessary

/**
 * Custom hook to fetch data from an API endpoint.
 * @param {string} url The API endpoint URL.
 * @param {Array<any>} dependencies Dependencies for the useEffect hook (e.g., to refetch).
 * @returns {{data: any, loading: boolean, error: string | null, refetch: Function}}
 */
export const useApiData = (url, dependencies = []) => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchData = async () => {
    setError(null); // Clear previous errors
    setLoading(true);
    try {
      const res = await apiFetch(url);

      if (!res.ok) {
        // Handle non-200 HTTP status codes
        // We throw an Error object here so it's caught by the catch block
        const errorMessage = `Failed to fetch data. Status: ${res.status}`;
        throw new Error(errorMessage);
      }

      const json = await res.json();
      setData(json);
    } catch (err) {
      // Handle network errors or the error thrown above
      console.error("Error fetching data:", err);
      // Use the thrown message or a default network error message
      setError(err.message || "Network error or server unreachable.");
      setData(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, dependencies); // Runs when dependencies change

  return { data, loading, error, refetch: fetchData };
};
