"use client";

import { useState } from "react";
import Link from "next/link";
import { useCart } from "@/lib/cart";
import { api, type Coupon } from "@/lib/api";
import { formatGel, GEL } from "@/lib/format";
import { useTranslation } from "@/lib/dictionary";

export default function CartPage() {
  const { items, total, count, setQty, remove, clear } = useCart();
  const { t } = useTranslation();
  const [placed, setPlaced] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [form, setForm] = useState({
    firstName: "",
    lastName: "",
    email: "",
    address: "",
    city: "",
    cardName: "",
    cardNumber: "",
    expiry: "",
    cvc: "",
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [couponInput, setCouponInput] = useState("");
  const [coupon, setCoupon] = useState<Coupon | null>(null);
  const [couponError, setCouponError] = useState<string | null>(null);
  const [couponBusy, setCouponBusy] = useState(false);

  const discount = coupon ? Math.round(total * coupon.percentOff) / 100 : 0;
  const net = Math.max(0, Math.round((total - discount) * 100) / 100);

  async function applyCoupon() {
    setCouponError(null);
    const code = couponInput.trim();
    if (!code) return;
    setCouponBusy(true);
    try {
      setCoupon(await api.coupon(code));
    } catch {
      setCoupon(null);
      setCouponError(t("coupon.invalid"));
    } finally {
      setCouponBusy(false);
    }
  }

  function removeCoupon() {
    setCoupon(null);
    setCouponInput("");
    setCouponError(null);
  }

  function update(field: string, value: string) {
    setForm((f) => ({ ...f, [field]: value }));
    setErrors((e) => ({ ...e, [field]: "" }));
  }

  // Card fields are formatted as the user types.
  function updateCard(field: "cardNumber" | "expiry" | "cvc", raw: string) {
    let v = raw;
    if (field === "cardNumber") {
      v = raw.replace(/\D/g, "").slice(0, 16).replace(/(.{4})/g, "$1 ").trim();
    } else if (field === "expiry") {
      const d = raw.replace(/\D/g, "").slice(0, 4);
      v = d.length > 2 ? `${d.slice(0, 2)}/${d.slice(2)}` : d;
    } else {
      v = raw.replace(/\D/g, "").slice(0, 4);
    }
    update(field, v);
  }

  async function checkout(e: React.FormEvent) {
    e.preventDefault();
    setSubmitError(null);
    const next: Record<string, string> = {};
    if (!form.firstName.trim()) next.firstName = "First name is required";
    if (!form.lastName.trim()) next.lastName = "Last name is required";
    if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(form.email)) next.email = "Enter a valid email";
    if (!form.address.trim()) next.address = "Address is required";
    if (!form.city.trim()) next.city = "City is required";

    if (!form.cardName.trim()) next.cardName = "Cardholder name is required";
    const cardDigits = form.cardNumber.replace(/\D/g, "");
    if (cardDigits.length !== 16) next.cardNumber = "Enter a 16-digit card number";
    if (!/^\d{2}\/\d{2}$/.test(form.expiry)) {
      next.expiry = "Use MM/YY";
    } else {
      const mm = Number(form.expiry.slice(0, 2));
      if (mm < 1 || mm > 12) next.expiry = "Invalid month";
    }
    if (!/^\d{3,4}$/.test(form.cvc)) next.cvc = "3–4 digits";

    if (Object.keys(next).length) {
      setErrors(next);
      return;
    }

    try {
      setSubmitting(true);
      const order = await api.checkout({
        firstName: form.firstName,
        lastName: form.lastName,
        email: form.email,
        address: form.address,
        city: form.city,
        items: items.map((i) => ({ productId: i.product.id, qty: i.qty })),
        couponCode: coupon?.code,
      });
      localStorage.setItem("luxshop_email", form.email);
      setPlaced(`#${order.orderNo}`);
      clear();
    } catch (err) {
      setSubmitError(err instanceof Error ? err.message : "Could not place the order. Please try again.");
    } finally {
      setSubmitting(false);
    }
  }

  if (placed) {
    return (
      <main className="container section">
        <div className="order-confirm">
          <div className="confirm-ico">✓</div>
          <h1 className="serif">{t("confirm.thanks", { name: form.firstName || "customer" })}</h1>
          <p>
            Your order <strong>{placed}</strong> has been placed and paid with the card ending in{" "}
            <strong>•• {form.cardNumber.replace(/\D/g, "").slice(-4)}</strong>. A confirmation was sent
            to <strong>{form.email}</strong>.
          </p>
          <div className="cta-row">
            <Link href="/orders" className="btn btn-navy">
              {t("confirm.viewOrders")}
            </Link>
            <Link href="/" className="btn btn-gold">
              {t("confirm.continue")}
            </Link>
          </div>
        </div>
      </main>
    );
  }

  return (
    <main className="container section">
      <h1 className="section-title" style={{ marginBottom: 6 }}>
        {t("cart.title")}
      </h1>
      <div className="section-sub" style={{ marginBottom: 24 }}>
        {t("cart.items", { n: count })}
      </div>

      {items.length === 0 ? (
        <div className="empty-cart">
          <p>{t("cart.empty")}</p>
          <Link href="/#catalog" className="btn btn-gold">
            {t("cart.browse")}
          </Link>
        </div>
      ) : (
        <div className="cart-layout">
          <div className="cart-items">
            {items.map(({ product, qty }) => (
              <div className="cart-item" key={product.id}>
                <div className="cart-thumb">
                  {product.imageUrl ? (
                    // eslint-disable-next-line @next/next/no-img-element
                    <img src={product.imageUrl} alt={product.productName} />
                  ) : (
                    <span className="disc" />
                  )}
                </div>
                <div className="cart-info">
                  <Link href={`/product/${product.id}`} className="cart-name serif">
                    {product.productName}
                  </Link>
                  <div className="cart-cat">{product.category?.name ?? "Uncategorized"}</div>
                  <div className="cart-price">
                    {formatGel(Number(product.price ?? 0))} <span className="gel">{GEL}</span>
                  </div>
                </div>
                <div className="cart-qty">
                  <button onClick={() => setQty(product.id, qty - 1)} aria-label="Decrease">
                    −
                  </button>
                  <span>{qty}</span>
                  <button onClick={() => setQty(product.id, qty + 1)} aria-label="Increase">
                    +
                  </button>
                </div>
                <div className="cart-line">
                  {formatGel(Number(product.price ?? 0) * qty)} <span className="gel">{GEL}</span>
                </div>
                <button className="cart-remove" onClick={() => remove(product.id)} aria-label="Remove">
                  ✕
                </button>
              </div>
            ))}
            <button className="link-btn" onClick={clear}>
              {t("cart.clear")}
            </button>
          </div>

          <form className="checkout" onSubmit={checkout} noValidate>
            <h3 className="serif">{t("checkout.title")}</h3>
            <div className="checkout-row">
              <label>
                {t("checkout.firstName")}
                <input value={form.firstName} onChange={(e) => update("firstName", e.target.value)} />
                {errors.firstName && <em>{errors.firstName}</em>}
              </label>
              <label>
                {t("checkout.lastName")}
                <input value={form.lastName} onChange={(e) => update("lastName", e.target.value)} />
                {errors.lastName && <em>{errors.lastName}</em>}
              </label>
            </div>
            <label>
              {t("checkout.email")}
              <input value={form.email} onChange={(e) => update("email", e.target.value)} />
              {errors.email && <em>{errors.email}</em>}
            </label>
            <label>
              {t("checkout.address")}
              <input value={form.address} onChange={(e) => update("address", e.target.value)} />
              {errors.address && <em>{errors.address}</em>}
            </label>
            <label>
              {t("checkout.city")}
              <input value={form.city} onChange={(e) => update("city", e.target.value)} />
              {errors.city && <em>{errors.city}</em>}
            </label>

            <div className="pay-head">
              <span>💳 {t("checkout.payment")}</span>
              <span className="pay-brands">VISA · MC · AMEX</span>
            </div>
            <label>
              {t("checkout.cardName")}
              <input
                value={form.cardName}
                onChange={(e) => update("cardName", e.target.value)}
                placeholder={t("checkout.cardNamePlaceholder")}
              />
              {errors.cardName && <em>{errors.cardName}</em>}
            </label>
            <label>
              {t("checkout.cardNumber")}
              <input
                value={form.cardNumber}
                onChange={(e) => updateCard("cardNumber", e.target.value)}
                inputMode="numeric"
                autoComplete="cc-number"
                placeholder="1234 5678 9012 3456"
              />
              {errors.cardNumber && <em>{errors.cardNumber}</em>}
            </label>
            <div className="checkout-row">
              <label>
                {t("checkout.expiry")}
                <input
                  value={form.expiry}
                  onChange={(e) => updateCard("expiry", e.target.value)}
                  inputMode="numeric"
                  autoComplete="cc-exp"
                  placeholder="MM/YY"
                />
                {errors.expiry && <em>{errors.expiry}</em>}
              </label>
              <label>
                {t("checkout.cvc")}
                <input
                  value={form.cvc}
                  onChange={(e) => updateCard("cvc", e.target.value)}
                  inputMode="numeric"
                  autoComplete="cc-csc"
                  placeholder="123"
                />
                {errors.cvc && <em>{errors.cvc}</em>}
              </label>
            </div>

            <div className="coupon-box">
              {coupon ? (
                <div className="coupon-applied">
                  <span>
                    🏷️ <strong>{coupon.code}</strong> · −{coupon.percentOff}%
                  </span>
                  <button type="button" className="link-btn" onClick={removeCoupon}>
                    {t("coupon.remove")}
                  </button>
                </div>
              ) : (
                <div className="coupon-row">
                  <input
                    value={couponInput}
                    onChange={(e) => {
                      setCouponInput(e.target.value);
                      setCouponError(null);
                    }}
                    placeholder={t("coupon.placeholder")}
                    aria-label={t("coupon.placeholder")}
                  />
                  <button type="button" className="btn btn-navy" onClick={applyCoupon} disabled={couponBusy}>
                    {couponBusy ? "…" : t("coupon.apply")}
                  </button>
                </div>
              )}
              {couponError && <em className="coupon-error">{couponError}</em>}
            </div>

            {coupon && (
              <>
                <div className="checkout-subtotal">
                  <span>{t("checkout.subtotal")}</span>
                  <span>
                    {formatGel(total)} <span className="gel">{GEL}</span>
                  </span>
                </div>
                <div className="checkout-subtotal discount">
                  <span>{t("coupon.discount", { code: coupon.code })}</span>
                  <span>
                    −{formatGel(discount)} <span className="gel">{GEL}</span>
                  </span>
                </div>
              </>
            )}
            <div className="checkout-total">
              <span>{t("checkout.total")}</span>
              <strong>
                {formatGel(net)} <span className="gel">{GEL}</span>
              </strong>
            </div>
            {submitError && <div className="checkout-error">{submitError}</div>}
            <button type="submit" className="btn btn-gold checkout-btn" disabled={submitting}>
              {submitting ? t("checkout.placing") : t("checkout.place")}
            </button>
          </form>
        </div>
      )}
    </main>
  );
}
