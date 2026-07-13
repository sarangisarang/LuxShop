"use client";

import { useEffect, useRef, useState } from "react";
import Link from "next/link";
import { api, type Product } from "@/lib/api";
import { formatGel, GEL } from "@/lib/format";
import { useCart } from "@/lib/cart";
import { useTranslation } from "@/lib/dictionary";

interface Msg {
  role: "user" | "assistant";
  text: string;
  products?: Product[];
}

// Render **bold** spans; everything else is plain text.
function RichText({ text }: { text: string }) {
  return (
    <>
      {text.split(/(\*\*[^*]+\*\*)/g).map((part, i) =>
        part.startsWith("**") && part.endsWith("**") ? (
          <strong key={i}>{part.slice(2, -2)}</strong>
        ) : (
          <span key={i}>{part}</span>
        )
      )}
    </>
  );
}

export default function AssistantWidget() {
  const { t } = useTranslation();
  const { add } = useCart();
  const [open, setOpen] = useState(false);
  const [input, setInput] = useState("");
  const [busy, setBusy] = useState(false);
  const [messages, setMessages] = useState<Msg[]>([]);
  const bodyRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    bodyRef.current?.scrollTo({ top: bodyRef.current.scrollHeight, behavior: "smooth" });
  }, [messages, busy]);

  async function send(e: React.FormEvent) {
    e.preventDefault();
    const q = input.trim();
    if (!q || busy) return;
    setInput("");
    setMessages((m) => [...m, { role: "user", text: q }]);
    setBusy(true);
    try {
      const reply = await api.assistant(q);
      setMessages((m) => [...m, { role: "assistant", text: reply.answer, products: reply.products }]);
    } catch {
      setMessages((m) => [...m, { role: "assistant", text: t("assistant.error") }]);
    } finally {
      setBusy(false);
    }
  }

  return (
    <>
      <button
        className={`assist-fab ${open ? "hidden" : ""}`}
        onClick={() => setOpen(true)}
        aria-label={t("assistant.open")}
      >
        ✨ {t("assistant.open")}
      </button>

      {open && (
        <div className="assist-panel" role="dialog" aria-label={t("assistant.title")}>
          <div className="assist-head">
            <div>
              <strong>{t("assistant.title")}</strong>
              <div className="assist-sub">{t("assistant.subtitle")}</div>
            </div>
            <button className="assist-close" onClick={() => setOpen(false)} aria-label="Close">
              ✕
            </button>
          </div>

          <div className="assist-body" ref={bodyRef}>
            {messages.length === 0 && <div className="assist-greeting">{t("assistant.greeting")}</div>}
            {messages.map((m, i) => (
              <div key={i} className={`assist-msg ${m.role}`}>
                <div className="assist-bubble">
                  <RichText text={m.text} />
                </div>
                {m.products && m.products.length > 0 && (
                  <div className="assist-cards">
                    {m.products.slice(0, 3).map((p) => (
                      <Link key={p.id} href={`/product/${p.id}`} className="assist-card">
                        <img
                          src={p.imageUrl ?? ""}
                          alt={p.productName}
                          onError={(e) => {
                            (e.currentTarget as HTMLImageElement).src = `https://picsum.photos/seed/${p.id}/80/80`;
                          }}
                        />
                        <span className="assist-card-name">{p.productName}</span>
                        <span className="assist-card-price">
                          {formatGel(Number(p.price ?? 0))} {GEL}
                        </span>
                        <button
                          type="button"
                          className="assist-card-add"
                          aria-label={t("card.add")}
                          title={t("card.add")}
                          disabled={Number(p.stock) <= 0}
                          onClick={(e) => {
                            e.preventDefault();
                            e.stopPropagation();
                            add(p);
                          }}
                        >
                          🛍
                        </button>
                      </Link>
                    ))}
                  </div>
                )}
              </div>
            ))}
            {busy && <div className="assist-msg assistant"><div className="assist-bubble typing">{t("assistant.thinking")}</div></div>}
          </div>

          <form className="assist-input" onSubmit={send}>
            <input
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder={t("assistant.placeholder")}
              aria-label={t("assistant.placeholder")}
            />
            <button type="submit" className="btn btn-gold" disabled={busy}>
              {t("assistant.send")}
            </button>
          </form>
        </div>
      )}
    </>
  );
}
