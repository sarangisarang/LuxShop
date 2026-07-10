"use client";

import { useEffect, useState } from "react";
import { admin, api, type Order, type Product } from "@/lib/api";
import { formatGel, GEL } from "@/lib/format";

const STATUS_ORDER = ["Pending", "Processing", "shipped", "closed"];
const STATUS_CLS: Record<string, string> = {
  Pending: "stock-low",
  Processing: "stock-ok",
  shipped: "stock-ok",
  closed: "stock-out",
};

export default function DashboardAdmin({ token }: { token: string }) {
  const [orders, setOrders] = useState<Order[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [catCount, setCatCount] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const [os, ps, cs] = await Promise.all([
          admin.orders(token),
          api.products(0, 100),
          api.categories(),
        ]);
        if (cancelled) return;
        setOrders(os);
        setProducts(ps);
        setCatCount(cs.length);
      } catch (e) {
        if (!cancelled) setError(e instanceof Error ? e.message : "Failed to load dashboard");
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [token]);

  const revenue = orders.reduce((s, o) => s + Number(o.orderTotal ?? 0), 0);
  const byStatus = orders.reduce<Record<string, number>>((m, o) => {
    m[o.orderStatus] = (m[o.orderStatus] ?? 0) + 1;
    return m;
  }, {});
  const rated = products.filter((p) => (p.reviewCount ?? 0) > 0);
  const avgRating = rated.length
    ? rated.reduce((s, p) => s + (p.averageRating ?? 0), 0) / rated.length
    : 0;
  const lowStock = products.filter((p) => Number(p.stock) > 0 && Number(p.stock) <= 3);
  const topRated = [...rated]
    .sort((a, b) => (b.averageRating ?? 0) - (a.averageRating ?? 0))
    .slice(0, 5);

  if (loading) return <div className="notice">Loading dashboard…</div>;
  if (error) return <div className="checkout-error">{error}</div>;

  const cards = [
    { label: "Revenue", value: `${formatGel(revenue)} ${GEL}` },
    { label: "Orders", value: String(orders.length) },
    { label: "Products", value: String(products.length) },
    { label: "Categories", value: String(catCount) },
    { label: "Avg. rating", value: avgRating ? avgRating.toFixed(2) : "—" },
    { label: "Low stock", value: String(lowStock.length) },
  ];

  return (
    <div>
      <div className="stat-grid">
        {cards.map((c) => (
          <div className="stat-card" key={c.label}>
            <span className="stat-value">{c.value}</span>
            <span className="stat-label">{c.label}</span>
          </div>
        ))}
      </div>

      <div className="dash-cols">
        <div className="dash-panel">
          <h4 className="serif">Orders by status</h4>
          {orders.length === 0 ? (
            <div className="section-sub">No orders yet.</div>
          ) : (
            STATUS_ORDER.filter((s) => byStatus[s]).map((s) => (
              <div className="dash-row" key={s}>
                <span className={`stock-pill ${STATUS_CLS[s] ?? "stock-ok"}`}>{s}</span>
                <span className="dash-count">{byStatus[s]}</span>
              </div>
            ))
          )}
        </div>

        <div className="dash-panel">
          <h4 className="serif">Top rated</h4>
          {topRated.length === 0 ? (
            <div className="section-sub">No reviews yet.</div>
          ) : (
            topRated.map((p) => (
              <div className="dash-row" key={p.id}>
                <span className="dash-name">{p.productName}</span>
                <span className="dash-count">
                  ★ {(p.averageRating ?? 0).toFixed(1)} ({p.reviewCount})
                </span>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}
