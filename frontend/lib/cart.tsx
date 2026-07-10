"use client";

import { createContext, useContext, useEffect, useState, ReactNode } from "react";
import type { Product } from "./api";

export interface CartItem {
  product: Product;
  qty: number;
}

interface CartContextValue {
  items: CartItem[];
  count: number;
  total: number;
  add: (product: Product, qty?: number) => void;
  remove: (id: string) => void;
  setQty: (id: string, qty: number) => void;
  clear: () => void;
}

const CartContext = createContext<CartContextValue | null>(null);
const STORAGE_KEY = "luxshop_cart";

export function CartProvider({ children }: { children: ReactNode }) {
  const [items, setItems] = useState<CartItem[]>([]);
  const [ready, setReady] = useState(false);

  // Load once on mount (client only).
  useEffect(() => {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (raw) setItems(JSON.parse(raw));
    } catch {
      /* ignore corrupt storage */
    }
    setReady(true);
  }, []);

  // Persist on change.
  useEffect(() => {
    if (ready) localStorage.setItem(STORAGE_KEY, JSON.stringify(items));
  }, [items, ready]);

  const add = (product: Product, qty = 1) =>
    setItems((prev) => {
      const idx = prev.findIndex((x) => x.product.id === product.id);
      if (idx >= 0) {
        const copy = [...prev];
        copy[idx] = { ...copy[idx], qty: copy[idx].qty + qty };
        return copy;
      }
      return [...prev, { product, qty }];
    });

  const remove = (id: string) => setItems((prev) => prev.filter((x) => x.product.id !== id));

  const setQty = (id: string, qty: number) =>
    setItems((prev) =>
      qty <= 0
        ? prev.filter((x) => x.product.id !== id)
        : prev.map((x) => (x.product.id === id ? { ...x, qty } : x))
    );

  const clear = () => setItems([]);

  const count = items.reduce((n, x) => n + x.qty, 0);
  const total = items.reduce((s, x) => s + Number(x.product.price ?? 0) * x.qty, 0);

  return (
    <CartContext.Provider value={{ items, count, total, add, remove, setQty, clear }}>
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  const ctx = useContext(CartContext);
  if (!ctx) throw new Error("useCart must be used within a CartProvider");
  return ctx;
}
