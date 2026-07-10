import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "LuxShop — Premium Store",
  description: "LuxShop e-commerce — built with Next.js & Spring Boot",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body>
        <nav className="nav">
          <div className="container" style={{ display: "flex", alignItems: "center", justifyContent: "space-between", width: "100%" }}>
            <div className="brand">
              Lux<span>Shop</span>
            </div>
            <div className="nav-links">
              <a href="/">Home</a>
              <a href="#catalog">Catalog</a>
              <a href="#">Cart</a>
              <a href="#">Sign in</a>
            </div>
          </div>
        </nav>
        {children}
        <footer className="footer">
          <div className="container">
            © {new Date().getFullYear()} LuxShop · Next.js + Spring Boot
          </div>
        </footer>
      </body>
    </html>
  );
}
