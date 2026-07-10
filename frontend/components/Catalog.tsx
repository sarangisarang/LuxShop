"use client";

import { useEffect, useRef, useState } from "react";
import { useSearchParams } from "next/navigation";
import { api, type Product } from "@/lib/api";
import { useLanguage } from "@/lib/language";
import { useTranslation } from "@/lib/dictionary";
import ProductCard from "./ProductCard";

export default function Catalog({ products: initial }: { products: Product[] }) {
  const params = useSearchParams();
  const { lang } = useLanguage();
  const { t } = useTranslation();

  const [products, setProducts] = useState<Product[]>(initial);
  const firstRun = useRef(true);

  // Re-fetch in the selected language (skip the first run — server already provided it).
  useEffect(() => {
    if (firstRun.current) {
      firstRun.current = false;
      return;
    }
    let cancelled = false;
    api
      .products(0, 100)
      .then((p) => {
        if (!cancelled) setProducts(p);
      })
      .catch(() => {});
    return () => {
      cancelled = true;
    };
  }, [lang]);

  // Filter by category id (stable across languages); chip labels use the localized name.
  const catList = [
    { id: "all", name: t("catalog.all") },
    ...Array.from(
      new Map(
        products.filter((p) => p.category).map((p) => [p.category!.id, p.category!.name])
      ).entries()
    ).map(([id, name]) => ({ id, name })),
  ];

  const initialActive =
    catList.find((c) => c.name === params.get("cat"))?.id ?? "all";
  const [active, setActive] = useState(initialActive);

  const shown = active === "all" ? products : products.filter((p) => p.category?.id === active);

  return (
    <>
      <div className="section-head">
        <div>
          <h2 className="section-title">{t("catalog.title")}</h2>
          <div className="section-sub">{t("catalog.subtitle")}</div>
        </div>
      </div>

      <div className="filter-bar">
        {catList.map((c) => (
          <button
            key={c.id}
            className={`chip ${c.id === active ? "active" : ""}`}
            onClick={() => setActive(c.id)}
          >
            {c.name}
          </button>
        ))}
      </div>

      <div className="grid">
        {shown.map((p, i) => (
          <ProductCard key={p.id} product={p} index={i} />
        ))}
      </div>

      {shown.length === 0 && <div className="notice">{t("catalog.empty")}</div>}
    </>
  );
}
