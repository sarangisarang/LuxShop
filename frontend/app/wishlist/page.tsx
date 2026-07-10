"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { api, type Product } from "@/lib/api";
import { useWishlist } from "@/lib/wishlist";
import { useLanguage } from "@/lib/language";
import { useTranslation } from "@/lib/dictionary";
import ProductCard from "@/components/ProductCard";

export default function WishlistPage() {
  const { ids, count, clear } = useWishlist();
  const { lang } = useLanguage();
  const { t } = useTranslation();
  const [products, setProducts] = useState<Product[]>([]);

  // Load the catalog (localized) and keep only the wishlisted items, in the
  // order they were added.
  useEffect(() => {
    let cancelled = false;
    api
      .products(0, 100)
      .then((all) => {
        if (cancelled) return;
        const byId = new Map(all.map((p) => [p.id, p]));
        setProducts(ids.map((id) => byId.get(id)).filter((p): p is Product => !!p));
      })
      .catch(() => {});
    return () => {
      cancelled = true;
    };
  }, [lang, ids]);

  return (
    <main className="container section">
      <div className="section-head">
        <div>
          <h1 className="section-title">{t("wishlist.title")}</h1>
          <div className="section-sub">{t("wishlist.subtitle")}</div>
        </div>
        {count > 0 && (
          <button className="btn btn-ghost" onClick={clear}>
            {t("wishlist.clear")}
          </button>
        )}
      </div>

      {count === 0 ? (
        <div className="notice">
          {t("wishlist.empty")}{" "}
          <Link href="/#catalog" className="link-gold">
            {t("wishlist.browse")}
          </Link>
        </div>
      ) : (
        <div className="grid">
          {products.map((p, i) => (
            <ProductCard key={p.id} product={p} index={i} />
          ))}
        </div>
      )}
    </main>
  );
}
