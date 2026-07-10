"use client";

import Link from "next/link";
import { useState } from "react";
import { AnimatePresence, motion } from "framer-motion";

const CATEGORIES = [
  { glyph: "💻", label: "Computers", href: "/#catalog" },
  { glyph: "📱", label: "Phones", href: "/#catalog" },
  { glyph: "📚", label: "Books", href: "/#catalog" },
  { glyph: "🎧", label: "Electronics", href: "/#catalog" },
];

export default function Navbar() {
  const [open, setOpen] = useState(false);

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
          <span className="icon-btn">👤 Account</span>
          <span className="nav-divider" />
          <span className="icon-btn">
            🛍 Cart <span className="cart-badge">0</span>
          </span>
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
                  href={c.href}
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
