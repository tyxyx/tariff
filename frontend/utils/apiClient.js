import Cookies from "js-cookie";

export async function apiFetch(url, options = {}) {
  const token = Cookies.get("auth_token");
  const combinedHeaders = {
    "Content-Type": "application/json",
    ...options.headers,
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };

  const response = await fetch(url, {
    ...options,
    headers: combinedHeaders,
    credentials: "include",
  });

  return response;
}
