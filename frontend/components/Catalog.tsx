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
  // Search box value; the committed query (debounced) is what actually hits the API.
  const [query, setQuery] = useState(params.get("q") ?? "");
  const [committed, setCommitted] = useState(params.get("q") ?? "");
  // "" = default order; other values map to the backend's friendly sort keys.
  const [sort, setSort] = useState("");
  const firstRun = useRef(true);

  // Debounce typing so we query the backend at most once the user pauses.
  useEffect(() => {
    const id = setTimeout(() => setCommitted(query), 300);
    return () => clearTimeout(id);
  }, [query]);

  // Re-fetch when the language, committed search term, or sort changes. The first
  // run is skipped only when there is nothing to change (no query, default sort);
  // the server already provided the unfiltered list.
  useEffect(() => {
    if (firstRun.current) {
      firstRun.current = false;
      if (!committed.trim() && !sort) return;
    }
    let cancelled = false;
    api
      .products(0, 100, committed, sort || undefined)
      .then((p) => {
        if (!cancelled) setProducts(p);
      })
      .catch(() => {});
    return () => {
      cancelled = true;
    };
  }, [lang, committed, sort]);

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
        <div className="search-box">
          <span className="search-icon" aria-hidden>🔎</span>
          <input
            className="search-input"
            type="search"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder={t("catalog.search")}
            aria-label={t("catalog.search")}
          />
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
        <select
          className="sort-select"
          value={sort}
          onChange={(e) => setSort(e.target.value)}
          aria-label={t("sort.label")}
        >
          <option value="">{t("sort.default")}</option>
          <option value="price_asc">{t("sort.priceAsc")}</option>
          <option value="price_desc">{t("sort.priceDesc")}</option>
          <option value="name_asc">{t("sort.nameAsc")}</option>
          <option value="name_desc">{t("sort.nameDesc")}</option>
        </select>
      </div>

      <div className="grid">
        {shown.map((p, i) => (
          <ProductCard key={p.id} product={p} index={i} />
        ))}
      </div>

      {shown.length === 0 && (
        <div className="notice">
          {committed.trim() ? t("catalog.noResults", { q: committed.trim() }) : t("catalog.empty")}
        </div>
      )}
    </>
  );
}
