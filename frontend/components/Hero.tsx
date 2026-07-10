"use client";

import { motion } from "framer-motion";

export default function Hero() {
  return (
    <section className="container hero">
      <motion.div
        initial={{ opacity: 0, x: -30 }}
        animate={{ opacity: 1, x: 0 }}
        transition={{ duration: 0.6, ease: "easeOut" }}
      >
        <span className="hero-eyebrow">LuxShop · Premium Store</span>
        <h1 className="serif">
          Timeless <span className="accent">Elegance</span>,
          <br /> delivered.
        </h1>
        <p>
          A curated selection of computers, phones and books — crafted shopping,
          powered by a Next.js storefront and a Spring Boot API.
        </p>
        <a className="btn btn-gold" href="#catalog">
          Shop Now →
        </a>
      </motion.div>

      <motion.div
        className="hero-art"
        initial={{ opacity: 0, scale: 0.9 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ duration: 0.7, ease: "easeOut", delay: 0.15 }}
      >
        <motion.span
          className="halo"
          animate={{ rotate: 360 }}
          transition={{ repeat: Infinity, duration: 24, ease: "linear" }}
        />
        <div className="glass">
          <div style={{ fontSize: "0.78rem", letterSpacing: "0.2em", opacity: 0.8 }}>
            FEATURED
          </div>
          <div className="serif" style={{ fontSize: "1.5rem", marginTop: 6 }}>
            MacBook Pro 16″
          </div>
          <div style={{ marginTop: 6, opacity: 0.85 }}>M2 Max · 32GB</div>
        </div>
      </motion.div>
    </section>
  );
}
