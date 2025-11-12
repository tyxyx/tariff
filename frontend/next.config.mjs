/** @type {import('next').NextConfig} */
const nextConfig = {
  output: "standalone",
  // 👇 Allow your LAN/dev IPs during development
  allowedDevOrigins: ["http://192.168.5.1:3000"],
};

export default nextConfig;
