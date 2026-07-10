"use client";

import { createContext, useContext, useEffect, useState, ReactNode } from "react";

export const LANGUAGES = [
  { code: "en", label: "English" },
  { code: "ka", label: "ქართული" },
  { code: "ru", label: "Русский" },
  { code: "de", label: "Deutsch" },
  { code: "fr", label: "Français" },
  { code: "es", label: "Español" },
  { code: "it", label: "Italiano" },
  { code: "pt", label: "Português" },
  { code: "tr", label: "Türkçe" },
  { code: "az", label: "Azərbaycan" },
  { code: "uk", label: "Українська" },
  { code: "pl", label: "Polski" },
  { code: "nl", label: "Nederlands" },
  { code: "ar", label: "العربية" },
  { code: "zh", label: "中文" },
  { code: "ja", label: "日本語" },
  { code: "ko", label: "한국어" },
  { code: "hi", label: "हिन्दी" },
] as const;

export type Lang = (typeof LANGUAGES)[number]["code"];
export const LANG_KEY = "luxshop_lang";
const CODES = LANGUAGES.map((l) => l.code) as readonly string[];

interface LanguageContextValue {
  lang: Lang;
  setLang: (l: Lang) => void;
}

const LanguageContext = createContext<LanguageContextValue | null>(null);

export function LanguageProvider({ children }: { children: ReactNode }) {
  const [lang, setLangState] = useState<Lang>("en");

  useEffect(() => {
    const saved = localStorage.getItem(LANG_KEY);
    if (saved && CODES.includes(saved)) setLangState(saved as Lang);
  }, []);

  const setLang = (l: Lang) => {
    localStorage.setItem(LANG_KEY, l);
    setLangState(l);
  };

  return <LanguageContext.Provider value={{ lang, setLang }}>{children}</LanguageContext.Provider>;
}

export function useLanguage() {
  const ctx = useContext(LanguageContext);
  if (!ctx) throw new Error("useLanguage must be used within a LanguageProvider");
  return ctx;
}
