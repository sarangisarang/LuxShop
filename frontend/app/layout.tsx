import type { Metadata } from "next";
import "./globals.css";
import Navbar from "@/components/Navbar";
import AssistantWidget from "@/components/AssistantWidget";
import { CartProvider } from "@/lib/cart";
import { WishlistProvider } from "@/lib/wishlist";
import { LanguageProvider } from "@/lib/language";
import { CurrencyProvider } from "@/lib/currency";
import { AuthProvider } from "@/lib/auth";

export const metadata: Metadata = {
  title: "LuxShop — Premium Store",
  description: "LuxShop e-commerce — Next.js storefront on a Spring Boot API",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>
        <LanguageProvider>
          <CurrencyProvider>
            <AuthProvider>
              <WishlistProvider>
                <CartProvider>
                <Navbar />
                {children}
                <AssistantWidget />
                <footer className="footer">
                  <div className="container">© 2026 LuxShop · Next.js + Spring Boot · Status: OK (JWT Auth)</div>
                </footer>
                </CartProvider>
              </WishlistProvider>
            </AuthProvider>
          </CurrencyProvider>
        </LanguageProvider>
      </body>
    </html>
  );
}
