"use client";

import { motion } from "framer-motion";
import { useTranslation } from "@/lib/dictionary";

export default function Hero() {
  const { t } = useTranslation();
  return (
    <section className="container hero">
      <motion.div
        initial={{ opacity: 0, x: -30 }}
        animate={{ opacity: 1, x: 0 }}
        transition={{ duration: 0.6, ease: "easeOut" }}
      >
        <span className="hero-eyebrow">{t("hero.eyebrow")}</span>
        <h1 className="serif">
          {t("hero.title1")} <span className="accent">{t("hero.titleAccent")}</span>,
          <br /> {t("hero.title2")}
        </h1>
        <p>{t("hero.subtitle")}</p>
        <a className="btn btn-gold" href="#catalog">
          {t("hero.cta")}
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
            {t("hero.featured")}
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
