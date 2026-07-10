"use client";

import { useEffect, useState } from "react";
import { api, type Product } from "@/lib/api";
import { useLanguage } from "@/lib/language";
import { useTranslation } from "@/lib/dictionary";
import ProductCard from "./ProductCard";

export default function TopRated() {
  const { lang } = useLanguage();
  const { t } = useTranslation();
  const [items, setItems] = useState<Product[]>([]);

  // Highest-rated products first; keep only those that actually have reviews.
  useEffect(() => {
    let cancelled = false;
    api
      .products(0, 8, undefined, "rating_desc")
      .then((ps) => {
        if (!cancelled) setItems(ps.filter((p) => (p.reviewCount ?? 0) > 0).slice(0, 4));
      })
      .catch(() => {});
    return () => {
      cancelled = true;
    };
  }, [lang]);

  if (items.length === 0) return null;

  return (
    <section className="container section top-rated">
      <div className="section-head">
        <div>
          <h2 className="section-title">{t("home.topRated")}</h2>
          <div className="section-sub">{t("home.topRatedSub")}</div>
        </div>
      </div>
      <div className="grid">
        {items.map((p, i) => (
          <ProductCard key={p.id} product={p} index={i} />
        ))}
      </div>
    </section>
  );
}
