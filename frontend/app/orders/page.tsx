"use client";

import { useEffect, useState } from "react";
import { api, type Order } from "@/lib/api";
import { useCurrency } from "@/lib/currency";
import { useTranslation } from "@/lib/dictionary";

const EMAIL_KEY = "luxshop_email";
const STATUS_CLS: Record<string, string> = {
  Pending: "stock-low",
  Processing: "stock-ok",
  shipped: "stock-ok",
  closed: "stock-out",
};

export default function OrdersPage() {
  const { t } = useTranslation();
  const { format } = useCurrency();
  const [email, setEmail] = useState("");
  const [orders, setOrders] = useState<Order[] | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [searched, setSearched] = useState(false);

  async function lookup(value: string) {
    const e = value.trim();
    if (!e) return;
    try {
      setLoading(true);
      setError(null);
      setSearched(true);
      const data = await api.ordersByEmail(e);
      setOrders(data);
      localStorage.setItem(EMAIL_KEY, e);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not load orders");
      setOrders(null);
    } finally {
      setLoading(false);
    }
  }

  // Prefill and auto-load from the email used at checkout.
  useEffect(() => {
    const saved = localStorage.getItem(EMAIL_KEY);
    if (saved) {
      setEmail(saved);
      lookup(saved);
    }
  }, []);

  return (
    <main className="container section">
      <h1 className="section-title" style={{ marginBottom: 6 }}>
        {t("orders.title")}
      </h1>
      <div className="section-sub" style={{ marginBottom: 24 }}>
        {t("orders.subtitle")}
      </div>

      <form
        className="orders-search"
        onSubmit={(ev) => {
          ev.preventDefault();
          lookup(email);
        }}
      >
        <input
          type="email"
          placeholder="you@example.com"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <button className="btn btn-gold" disabled={loading}>
          {loading ? t("orders.searching") : t("orders.find")}
        </button>
      </form>

      {error && (
        <div className="checkout-error" style={{ marginTop: 16 }}>
          {error}
        </div>
      )}

      {searched && !loading && orders && orders.length === 0 && (
        <div className="notice" style={{ marginTop: 20 }}>
          {t("orders.none")}
        </div>
      )}

      <div className="orders-list">
        {orders?.map((o) => (
          <div className="order-card" key={o.id}>
            <div className="order-head">
              <div className="order-meta">
                <span className="order-no">#{o.orderNo}</span>
                <span className="order-date">{o.orderDate}</span>
              </div>
              <span className={`stock-pill ${STATUS_CLS[o.orderStatus] ?? "stock-ok"}`}>
                {o.orderStatus}
              </span>
            </div>
            <div className="order-lines">
              {o.details.map((d, i) => (
                <div className="order-line" key={i}>
                  <span>
                    {d.qty} × {d.productName}
                  </span>
                  <span>
                    {format(d.subtotal)}
                  </span>
                </div>
              ))}
            </div>
            <div className="order-total">
              <span>{t("orders.total")}</span>
              <strong>
                {format(o.orderTotal)}
              </strong>
            </div>
          </div>
        ))}
      </div>
    </main>
  );
}
