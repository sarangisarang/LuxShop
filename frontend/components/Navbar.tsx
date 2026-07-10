"use client";

import Link from "next/link";
import { useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { useCart } from "@/lib/cart";

const CATEGORIES = [
  { glyph: "⌚", label: "Watches" },
  { glyph: "💻", label: "Laptops" },
  { glyph: "📱", label: "Smartphones" },
  { glyph: "🎧", label: "Audio" },
  { glyph: "👜", label: "Accessories" },
];

function catHref(label: string) {
  return `/?cat=${encodeURIComponent(label)}#catalog`;
}

export default function Navbar() {
  const [open, setOpen] = useState(false);
  const { count } = useCart();

  return (
    <nav className="nav" onMouseLeave={() => setOpen(false)}>
      <div className="container nav-inner">
        <Link href="/" className="brand">
          <span className="lux">Lux</span>Shop
        </Link>

        <div className="nav-links">
          <Link href="/" className="nav-link">
            Home
          </Link>
          <span
            className={`nav-link ${open ? "open" : ""}`}
            onMouseEnter={() => setOpen(true)}
            onClick={() => setOpen((v) => !v)}
          >
            Shop <span className="chev">▾</span>
          </span>
          <Link href="/#catalog" className="nav-link">
            Menu
          </Link>
        </div>

        <div className="nav-right">
          <Link href="/orders" className="icon-btn">
            📦 Orders
          </Link>
          <span className="nav-divider" />
          <Link href="/cart" className="icon-btn">
            🛍 Cart <span className="cart-badge">{count}</span>
          </Link>
        </div>
      </div>

      {/* Morphing category dropdown */}
      <AnimatePresence>
        {open && (
          <div className="dropdown-wrap">
            <motion.div
              className="dropdown"
              initial={{ opacity: 0, y: -14, scale: 0.96 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              exit={{ opacity: 0, y: -14, scale: 0.96 }}
              transition={{ type: "spring", stiffness: 320, damping: 26 }}
              onMouseEnter={() => setOpen(true)}
            >
              {CATEGORIES.map((c, i) => (
                <motion.a
                  key={c.label}
                  href={catHref(c.label)}
                  className="dropdown-item"
                  initial={{ opacity: 0, y: 8 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.04 * i }}
                  onClick={() => setOpen(false)}
                >
                  <span className="glyph">{c.glyph}</span>
                  <span className="label">{c.label}</span>
                </motion.a>
              ))}
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </nav>
  );
}
