"use client";

import { useCallback, useEffect, useState } from "react";
import { admin, type Order, type OrderAction } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { useCurrency } from "@/lib/currency";
import ProductsAdmin from "./ProductsAdmin";
import CategoriesAdmin from "./CategoriesAdmin";
import DashboardAdmin from "./DashboardAdmin";

const STATUS_CLS: Record<string, string> = {
  Pending: "stock-low",
  Processing: "stock-ok",
  shipped: "stock-ok",
  closed: "stock-out",
};

// Which actions are offered for each status (mirrors the backend state machine).
function actionsFor(status: string): { action: OrderAction; label: string }[] {
  switch (status) {
    case "Pending":
      return [{ action: "process", label: "Process" }];
    case "Processing":
      return [
        { action: "ship", label: "Ship" },
        { action: "pending", label: "Revert" },
      ];
    case "shipped":
      return [{ action: "close", label: "Close" }];
    default:
      return [];
  }
}

export default function AdminPage() {
  const { token, username, login, logout } = useAuth();
  const { format } = useCurrency();
  const [form, setForm] = useState({ username: "admin", password: "" });
  const [loginError, setLoginError] = useState<string | null>(null);
  const [orders, setOrders] = useState<Order[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState<string | null>(null);
  const [tab, setTab] = useState<"dashboard" | "orders" | "products" | "categories">("dashboard");
  const TITLES = {
    dashboard: "Dashboard",
    orders: "Order management",
    products: "Product management",
    categories: "Category management",
  };

  const loadOrders = useCallback(async (tk: string) => {
    try {
      setError(null);
      const data = await admin.orders(tk);
      setOrders([...data].sort((a, b) => b.orderNo - a.orderNo));
    } catch (e) {
      setError(e instanceof Error ? e.message : "Failed to load orders");
    }
  }, []);

  useEffect(() => {
    if (token) loadOrders(token);
  }, [token, loadOrders]);

  async function doLogin(e: React.FormEvent) {
    e.preventDefault();
    setLoginError(null);
    try {
      await login(form.username, form.password);
    } catch (err) {
      setLoginError(err instanceof Error ? err.message : "Login failed");
    }
  }

  async function changeStatus(id: string, action: OrderAction) {
    if (!token) return;
    try {
      setBusy(id + action);
      await admin.setStatus(token, id, action);
      await loadOrders(token);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Action failed");
    } finally {
      setBusy(null);
    }
  }

  if (!token) {
    return (
      <main className="container section">
        <h1 className="section-title" style={{ marginBottom: 24 }}>
          Admin sign in
        </h1>
        <form className="checkout admin-login" onSubmit={doLogin}>
          <label>
            Username
            <input value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} />
          </label>
          <label>
            Password
            <input
              type="password"
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
            />
          </label>
          {loginError && <div className="checkout-error">{loginError}</div>}
          <button type="submit" className="btn btn-gold">
            Sign in
          </button>
          <div className="section-sub">Demo admin: admin / 1234</div>
        </form>
      </main>
    );
  }

  return (
    <main className="container section">
      <div className="admin-head">
        <div>
          <h1 className="section-title" style={{ marginBottom: 4 }}>
            {TITLES[tab]}
          </h1>
          <div className="section-sub">Signed in as {username}</div>
        </div>
        <button className="btn btn-ghost" onClick={logout}>
          Sign out
        </button>
      </div>

      <div className="filter-bar" style={{ marginBottom: 24 }}>
        <button
          className={`chip ${tab === "dashboard" ? "active" : ""}`}
          onClick={() => setTab("dashboard")}
        >
          Dashboard
        </button>
        <button
          className={`chip ${tab === "orders" ? "active" : ""}`}
          onClick={() => setTab("orders")}
        >
          Orders
        </button>
        <button
          className={`chip ${tab === "products" ? "active" : ""}`}
          onClick={() => setTab("products")}
        >
          Products
        </button>
        <button
          className={`chip ${tab === "categories" ? "active" : ""}`}
          onClick={() => setTab("categories")}
        >
          Categories
        </button>
      </div>

      {tab === "dashboard" ? (
        <DashboardAdmin token={token} />
      ) : tab === "products" ? (
        <ProductsAdmin token={token} />
      ) : tab === "categories" ? (
        <CategoriesAdmin token={token} />
      ) : (
        <>
          {error && <div className="checkout-error" style={{ marginBottom: 16 }}>{error}</div>}

          <div className="orders-list">
        {orders.map((o) => (
          <div className="order-card" key={o.id}>
            <div className="order-head">
              <div className="order-meta">
                <span className="order-no">#{o.orderNo}</span>
                <span className="order-date">{o.orderDate}</span>
                <span className="order-date">{o.customer?.email ?? "—"}</span>
              </div>
              <span className={`stock-pill ${STATUS_CLS[o.orderStatus] ?? "stock-ok"}`}>{o.orderStatus}</span>
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
              <div className="admin-actions">
                {actionsFor(o.orderStatus).map(({ action, label }) => (
                  <button
                    key={action}
                    className="btn btn-navy admin-btn"
                    disabled={busy === o.id + action}
                    onClick={() => changeStatus(o.id, action)}
                  >
                    {busy === o.id + action ? "…" : label}
                  </button>
                ))}
                {actionsFor(o.orderStatus).length === 0 && (
                  <span className="section-sub">No further actions</span>
                )}
              </div>
              <strong>
                {format(o.orderTotal)}
              </strong>
            </div>
          </div>
        ))}
            {orders.length === 0 && <div className="notice">No orders yet.</div>}
          </div>
        </>
      )}
    </main>
  );
}
