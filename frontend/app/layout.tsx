import type { Metadata } from "next";
import "./globals.css";
import Navbar from "@/components/Navbar";

export const metadata: Metadata = {
  title: "LuxShop — Premium Store",
  description: "LuxShop e-commerce — Next.js storefront on a Spring Boot API",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>
        <Navbar />
        {children}
        <footer className="footer">
          <div className="container">© 2026 LuxShop · Next.js + Spring Boot · Status: OK (BCrypt Auth)</div>
        </footer>
      </body>
    </html>
  );
}
