"use client";

import { useEffect, useState } from "react";
import { api, type Product } from "@/lib/api";
import { getRecentIds } from "@/lib/recentlyViewed";
import { useLanguage } from "@/lib/language";
import { useTranslation } from "@/lib/dictionary";
import ProductCard from "./ProductCard";

export default function RecentlyViewed() {
  const { lang } = useLanguage();
  const { t } = useTranslation();
  const [items, setItems] = useState<Product[]>([]);

  // Load the products the shopper recently viewed (localized), newest first.
  useEffect(() => {
    const ids = getRecentIds();
    if (ids.length === 0) return;
    let cancelled = false;
    api
      .products(0, 100)
      .then((all) => {
        if (cancelled) return;
        const byId = new Map(all.map((p) => [p.id, p]));
        setItems(
          ids.map((id) => byId.get(id)).filter((p): p is Product => !!p).slice(0, 6)
        );
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
          <h2 className="section-title">{t("home.recentlyViewed")}</h2>
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
