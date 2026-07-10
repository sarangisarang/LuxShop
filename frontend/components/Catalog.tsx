"use client";

import { useState } from "react";
import { useSearchParams } from "next/navigation";
import type { Product } from "@/lib/api";
import ProductCard from "./ProductCard";

export default function Catalog({ products }: { products: Product[] }) {
  const params = useSearchParams();
  const initial = params.get("cat") ?? "All";

  const categories = [
    "All",
    ...Array.from(new Set(products.map((p) => p.category?.name).filter(Boolean) as string[])),
  ];
  const [active, setActive] = useState(categories.includes(initial) ? initial : "All");

  const shown = active === "All" ? products : products.filter((p) => p.category?.name === active);

  return (
    <>
      <div className="filter-bar">
        {categories.map((c) => (
          <button
            key={c}
            className={`chip ${c === active ? "active" : ""}`}
            onClick={() => setActive(c)}
          >
            {c}
          </button>
        ))}
      </div>

      <div className="grid">
        {shown.map((p, i) => (
          <ProductCard key={p.id} product={p} index={i} />
        ))}
      </div>

      {shown.length === 0 && <div className="notice">No products in this category yet.</div>}
    </>
  );
}
