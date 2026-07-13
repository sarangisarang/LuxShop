"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useRef, useState } from "react";
import { motion } from "framer-motion";
import { api, type Product } from "@/lib/api";
import { useCurrency } from "@/lib/currency";
import { useCart } from "@/lib/cart";
import { useLanguage } from "@/lib/language";
import { useTranslation } from "@/lib/dictionary";
import { recordView } from "@/lib/recentlyViewed";
import Reviews from "./Reviews";
import ProductCard from "./ProductCard";

export default function ProductDetail({ product: initial }: { product: Product }) {
  const { add } = useCart();
  const { lang } = useLanguage();
  const { t } = useTranslation();
  const { format } = useCurrency();
  const router = useRouter();
  const [product, setProduct] = useState(initial);
  const [related, setRelated] = useState<Product[]>([]);
  const [added, setAdded] = useState(false);
  const [activeImg, setActiveImg] = useState(0);
  const firstRun = useRef(true);

  // Gallery: the product's images, or just its primary image as a fallback.
  const gallery = (product.images && product.images.length > 0
    ? product.images
    : [product.imageUrl]
  ).filter((u): u is string => !!u);
  const mainImage = gallery[Math.min(activeImg, gallery.length - 1)];

  // Remember this product for the "Recently viewed" row.
  useEffect(() => {
    recordView(initial.id);
  }, [initial.id]);

  // Fetch "you may also like" products (localized), refreshing on language change.
  useEffect(() => {
    let cancelled = false;
    api
      .related(initial.id)
      .then((r) => {
        if (!cancelled) setRelated(r);
      })
      .catch(() => {});
    return () => {
      cancelled = true;
    };
  }, [lang, initial.id]);

  // Re-fetch the product in the selected language.
  useEffect(() => {
    if (firstRun.current) {
      firstRun.current = false;
      return;
    }
    let cancelled = false;
    api
      .product(initial.id)
      .then((p) => {
        if (!cancelled) setProduct(p);
      })
      .catch(() => {});
    return () => {
      cancelled = true;
    };
  }, [lang, initial.id]);

  const stock = Number(product.stock);
  const outOfStock = stock <= 0;
  const pill =
    stock <= 0
      ? { cls: "stock-out", text: t("stock.out") }
      : stock <= 3
        ? { cls: "stock-low", text: t("stock.only", { n: stock }) }
        : { cls: "stock-ok", text: t("stock.in", { n: stock }) };
  const specs: [string, string][] = [
    [t("pdp.category"), product.category?.name ?? "—"],
    [t("pdp.product"), product.productName],
    ["SKU", product.id],
    [t("pdp.availability"), stock > 0 ? t("pdp.inStock") : t("pdp.backorder")],
    [t("pdp.warranty"), t("pdp.warrantyVal")],
  ];

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
        <Link href="/">{t("nav.home")}</Link>
        <span className="sep">›</span>
        <Link href="/#catalog">{product.category?.name ?? t("nav.shop")}</Link>
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
          {mainImage ? (
            <>
              <img
                className="gallery-img"
                src={mainImage}
                alt={product.productName}
                onError={(e) => {
                  (e.currentTarget as HTMLImageElement).src = `https://picsum.photos/seed/${product.id}/900/700`;
                }}
              />
              {gallery.length > 1 && (
                <div className="gallery-thumbs">
                  {gallery.map((url, i) => (
                    <button
                      key={i}
                      type="button"
                      className={`gallery-thumb ${i === activeImg ? "active" : ""}`}
                      onClick={() => setActiveImg(i)}
                      aria-label={`Image ${i + 1}`}
                    >
                      <img
                        src={url}
                        alt=""
                        onError={(e) => {
                          (e.currentTarget as HTMLImageElement).src = `https://picsum.photos/seed/${product.id}-${i}/160/160`;
                        }}
                      />
                    </button>
                  ))}
                </div>
              )}
            </>
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
              {format(Number(product.price ?? 0))}
            </span>
            <span className={`stock-pill ${pill.cls}`}>{pill.text}</span>
          </div>

          <p style={{ color: "var(--muted)", marginBottom: 24 }}>{product.productDesc}</p>

          <div className="cta-row">
            <button className="btn btn-gold" disabled={outOfStock} onClick={addToCart}>
              {added ? t("pdp.added") : t("pdp.addToCart")}
            </button>
            <button className="btn btn-navy" disabled={outOfStock} onClick={buyNow}>
              {t("pdp.buyNow")}
            </button>
          </div>

          <table className="spec-table">
            <tbody>
              {specs.map(([k, v]) => (
                <tr key={k}>
                  <td>{k}</td>
                  <td>{v}</td>
                </tr>
              ))}
            </tbody>
          </table>

          <div className="ai-card">
            <h4 className="serif">{t("pdp.aiTitle")}</h4>
            <div className="ai-row">
              <span className="ai-ico">🎁</span>
              <span>{t("pdp.ai1")}</span>
            </div>
            <div className="ai-row">
              <span className="ai-ico">🖱️</span>
              <span>{t("pdp.ai2")}</span>
            </div>
          </div>
        </motion.div>
      </div>

      {related.length > 0 && (
        <section className="related">
          <h3 className="serif">{t("pdp.related")}</h3>
          <div className="grid">
            {related.map((p, i) => (
              <ProductCard key={p.id} product={p} index={i} />
            ))}
          </div>
        </section>
      )}

      <Reviews productId={product.id} />
    </div>
  );
}
