"use client";

import Link from "next/link";
import { motion } from "framer-motion";
import type { Product } from "@/lib/api";
import { useCurrency } from "@/lib/currency";
import { useCart } from "@/lib/cart";
import { useWishlist } from "@/lib/wishlist";
import { useTranslation } from "@/lib/dictionary";

export default function ProductCard({ product, index = 0 }: { product: Product; index?: number }) {
  const { add } = useCart();
  const { has, toggle } = useWishlist();
  const { t } = useTranslation();
  const { format } = useCurrency();
  const wished = has(product.id);
  const stock = Number(product.stock);
  const outOfStock = stock <= 0;
  const pill =
    stock <= 0
      ? { cls: "stock-out", text: t("stock.out") }
      : stock <= 3
        ? { cls: "stock-low", text: t("stock.only", { n: stock }) }
        : { cls: "stock-ok", text: t("stock.in", { n: stock }) };

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
          <button
            className={`fav-btn ${wished ? "on" : ""}`}
            aria-pressed={wished}
            aria-label={wished ? t("card.unfav") : t("card.fav")}
            title={wished ? t("card.unfav") : t("card.fav")}
            onClick={(e) => {
              e.preventDefault();
              e.stopPropagation();
              toggle(product.id);
            }}
          >
            {wished ? "♥" : "♡"}
          </button>
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
          {(product.reviewCount ?? 0) > 0 && (
            <div className="card-rating" aria-label={t("reviews.count", { n: product.reviewCount ?? 0 })}>
              <span className="stars" aria-hidden>
                {[1, 2, 3, 4, 5].map((i) => {
                  const on = i <= Math.round(product.averageRating ?? 0);
                  return (
                    <span key={i} className={on ? "star on" : "star"}>
                      {on ? "★" : "☆"}
                    </span>
                  );
                })}
              </span>
              <span className="card-rating-meta">
                {(product.averageRating ?? 0).toFixed(1)} ({product.reviewCount})
              </span>
            </div>
          )}
          <div className="desc">{product.productDesc}</div>
          <div className="row">
            <span className="price">
              {format(Number(product.price ?? 0))}
            </span>
            <span className={`stock-pill ${pill.cls}`}>{pill.text}</span>
          </div>
          <button
            className="add-btn"
            disabled={outOfStock}
            onClick={(e) => {
              e.preventDefault();
              e.stopPropagation();
              add(product);
            }}
          >
            {outOfStock ? t("stock.out") : t("card.add")}
          </button>
        </div>
      </Link>
    </motion.div>
  );
}
