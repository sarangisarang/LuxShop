"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { motion } from "framer-motion";
import type { Product } from "@/lib/api";
import { formatGel, GEL } from "@/lib/format";
import { useCart } from "@/lib/cart";

function stockPill(stock: number) {
  if (stock <= 0) return { cls: "stock-out", text: "Out of stock" };
  if (stock <= 3) return { cls: "stock-low", text: `Only ${stock} units left in stock!` };
  return { cls: "stock-ok", text: `${stock} in stock` };
}

// The backend product model is intentionally sparse; derive a small spec list
// from the fields we have so the page matches the reference layout.
function specs(product: Product): [string, string][] {
  return [
    ["Category", product.category?.name ?? "—"],
    ["Product", product.productName],
    ["SKU", product.id],
    ["Availability", Number(product.stock) > 0 ? "In stock" : "Backorder"],
    ["Warranty", "24 months"],
  ];
}

export default function ProductDetail({ product }: { product: Product }) {
  const pill = stockPill(Number(product.stock));
  const { add } = useCart();
  const router = useRouter();
  const [added, setAdded] = useState(false);
  const outOfStock = Number(product.stock) <= 0;

  function addToCart() {
    add(product);
    setAdded(true);
    setTimeout(() => setAdded(false), 1800);
  }

  function buyNow() {
    add(product);
    router.push("/cart");
  }

  return (
    <div className="container">
      <div className="breadcrumb">
        <Link href="/">Home</Link>
        <span className="sep">›</span>
        <Link href="/#catalog">{product.category?.name ?? "Shop"}</Link>
        <span className="sep">›</span>
        <span className="current">{product.productName}</span>
      </div>

      <div className="pdp">
        <motion.div
          className="gallery"
          initial={{ opacity: 0, scale: 0.92 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.6, ease: "easeOut" }}
        >
          {product.imageUrl ? (
            <img
              className="gallery-img"
              src={product.imageUrl}
              alt={product.productName}
              onError={(e) => {
                (e.currentTarget as HTMLImageElement).src = `https://picsum.photos/seed/${product.id}/900/700`;
              }}
            />
          ) : (
            <motion.span
              className="disc"
              animate={{ rotate: 360 }}
              transition={{ repeat: Infinity, duration: 26, ease: "linear" }}
            />
          )}
        </motion.div>

        <motion.div
          initial={{ opacity: 0, x: 30 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.55, ease: "easeOut", delay: 0.1 }}
        >
          <h1 className="serif">{product.productName}</h1>

          <div className="pdp-price-row">
            <span className="pdp-price">
              {formatGel(Number(product.price ?? 0))} <span className="gel">{GEL}</span>
            </span>
            <span className={`stock-pill ${pill.cls}`}>{pill.text}</span>
          </div>

          <p style={{ color: "var(--muted)", marginBottom: 24 }}>{product.productDesc}</p>

          <div className="cta-row">
            <button className="btn btn-gold" disabled={outOfStock} onClick={addToCart}>
              {added ? "✓ Added" : "Add to Cart"}
            </button>
            <button className="btn btn-navy" disabled={outOfStock} onClick={buyNow}>
              Buy Now
            </button>
          </div>

          <table className="spec-table">
            <tbody>
              {specs(product).map(([k, v]) => (
                <tr key={k}>
                  <td>{k}</td>
                  <td>{v}</td>
                </tr>
              ))}
            </tbody>
          </table>

          <div className="ai-card">
            <h4 className="serif">AI Recommendations</h4>
            <div className="ai-row">
              <span className="ai-ico">🎁</span>
              <span>Premium leather sleeve — frequently bought together</span>
            </div>
            <div className="ai-row">
              <span className="ai-ico">🖱️</span>
              <span>Wireless mouse — pairs well with this item</span>
            </div>
          </div>
        </motion.div>
      </div>
    </div>
  );
}
