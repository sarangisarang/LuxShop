"use client";

import { createContext, useContext, useEffect, useState, ReactNode } from "react";
import { api } from "./api";

export interface CurrencyOption {
  code: string;
  symbol: string;
  prefix: boolean; // true → "$285", false → "749 GEL"
  label: string;
}

// Only well-rendering symbols are used as prefixes; the rest show their code.
export const CURRENCIES: CurrencyOption[] = [
  { code: "GEL", symbol: "GEL", prefix: false, label: "GEL · ლარი" },
  { code: "USD", symbol: "$", prefix: true, label: "USD · $" },
  { code: "EUR", symbol: "€", prefix: true, label: "EUR · €" },
  { code: "GBP", symbol: "£", prefix: true, label: "GBP · £" },
  { code: "TRY", symbol: "TRY", prefix: false, label: "TRY · ₺" },
];

interface CurrencyContextValue {
  currency: string;
  setCurrency: (code: string) => void;
  format: (gelAmount: number) => string;
  updated: string | null;
}

const CurrencyContext = createContext<CurrencyContextValue | null>(null);
const STORAGE_KEY = "luxshop_currency";

export function CurrencyProvider({ children }: { children: ReactNode }) {
  const [currency, setCurrencyState] = useState("GEL");
  const [rates, setRates] = useState<Record<string, number>>({ GEL: 1 });
  const [updated, setUpdated] = useState<string | null>(null);

  useEffect(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      if (saved) setCurrencyState(saved);
    } catch {
      /* ignore */
    }
    // Live rates (base GEL) from the backend proxy; GEL is always 1.
    api
      .rates()
      .then((r) => {
        setRates({ ...r.rates, GEL: 1 });
        setUpdated(r.updated);
      })
      .catch(() => {});
  }, []);

  function setCurrency(code: string) {
    setCurrencyState(code);
    try {
      localStorage.setItem(STORAGE_KEY, code);
    } catch {
      /* ignore */
    }
  }

  function format(gelAmount: number): string {
    const rate = rates[currency] ?? 1;
    const value = (Number(gelAmount) || 0) * rate;
    const opt = CURRENCIES.find((c) => c.code === currency) ?? CURRENCIES[0];
    const num = new Intl.NumberFormat("en-US", { maximumFractionDigits: 0 }).format(value);
    return opt.prefix ? `${opt.symbol}${num}` : `${num} ${opt.symbol}`;
  }

  return (
    <CurrencyContext.Provider value={{ currency, setCurrency, format, updated }}>
      {children}
    </CurrencyContext.Provider>
  );
}

export function useCurrency() {
  const ctx = useContext(CurrencyContext);
  if (!ctx) throw new Error("useCurrency must be used within a CurrencyProvider");
  return ctx;
}
