/** @type {import('next').NextConfig} */
const nextConfig = {
  // Produce a self-contained server build for a small Docker runtime image.
  output: "standalone",
  async rewrites() {
    // Proxy API calls to the Spring Boot backend during development.
    const backend = process.env.BACKEND_URL || "http://localhost:8080";
    return [
      {
        source: "/api/:path*",
        destination: `${backend}/:path*`,
      },
    ];
  },
};

export default nextConfig;
