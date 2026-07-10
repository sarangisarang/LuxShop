"use client";

import Link from "next/link";
import { motion } from "framer-motion";
import type { Product } from "@/lib/api";
import { formatGel, GEL } from "@/lib/format";

function stockPill(stock: number) {
  if (stock <= 0) return { cls: "stock-out", text: "Out of stock" };
  if (stock <= 3) return { cls: "stock-low", text: `Only ${stock} left!` };
  return { cls: "stock-ok", text: `${stock} in stock` };
}

export default function ProductCard({ product, index = 0 }: { product: Product; index?: number }) {
  const pill = stockPill(Number(product.stock));

  return (
    <motion.div
      initial={{ opacity: 0, y: 24 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, margin: "-40px" }}
      transition={{ duration: 0.45, delay: Math.min(index * 0.06, 0.4) }}
      whileHover={{ y: -6, boxShadow: "0 24px 60px rgba(22,41,77,0.18)" }}
    >
      <Link href={`/product/${product.id}`} className="pcard" style={{ display: "flex" }}>
        <div className="pcard-media">
          {product.imageUrl ? (
            <img
              className="pcard-img"
              src={product.imageUrl}
              alt={product.productName}
              loading="lazy"
              onError={(e) => {
                (e.currentTarget as HTMLImageElement).src = `https://picsum.photos/seed/${product.id}/600/400`;
              }}
            />
          ) : (
            <span className="disc" />
          )}
        </div>
        <div className="pcard-body">
          <span className="cat">{product.category?.name ?? "Uncategorized"}</span>
          <h3 className="serif">{product.productName}</h3>
          <div className="desc">{product.productDesc}</div>
          <div className="row">
            <span className="price">
              {formatGel(Number(product.price ?? 0))} <span className="gel">{GEL}</span>
            </span>
            <span className={`stock-pill ${pill.cls}`}>{pill.text}</span>
          </div>
        </div>
      </Link>
    </motion.div>
  );
}
