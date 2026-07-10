import type { Metadata } from "next";
import "./globals.css";
import Navbar from "@/components/Navbar";
import { CartProvider } from "@/lib/cart";

export const metadata: Metadata = {
  title: "LuxShop — Premium Store",
  description: "LuxShop e-commerce — Next.js storefront on a Spring Boot API",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>
        <CartProvider>
          <Navbar />
          {children}
          <footer className="footer">
            <div className="container">© 2026 LuxShop · Next.js + Spring Boot · Status: OK (JWT Auth)</div>
          </footer>
        </CartProvider>
      </body>
    </html>
  );
}
