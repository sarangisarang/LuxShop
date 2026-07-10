"use client";

import { useState } from "react";
import Link from "next/link";
import { useCart } from "@/lib/cart";
import { api } from "@/lib/api";
import { formatGel, GEL } from "@/lib/format";

export default function CartPage() {
  const { items, total, count, setQty, remove, clear } = useCart();
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
      });
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
          <h1 className="serif">Thank you, {form.firstName || "customer"}!</h1>
          <p>
            Your order <strong>{placed}</strong> has been placed and paid with the card ending in{" "}
            <strong>•• {form.cardNumber.replace(/\D/g, "").slice(-4)}</strong>. A confirmation was sent
            to <strong>{form.email}</strong>.
          </p>
          <Link href="/" className="btn btn-gold">
            Continue shopping
          </Link>
        </div>
      </main>
    );
  }

  return (
    <main className="container section">
      <h1 className="section-title" style={{ marginBottom: 6 }}>
        Your Cart
      </h1>
      <div className="section-sub" style={{ marginBottom: 24 }}>
        {count} item{count === 1 ? "" : "s"}
      </div>

      {items.length === 0 ? (
        <div className="empty-cart">
          <p>Your cart is empty.</p>
          <Link href="/#catalog" className="btn btn-gold">
            Browse products
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
              Clear cart
            </button>
          </div>

          <form className="checkout" onSubmit={checkout} noValidate>
            <h3 className="serif">Checkout</h3>
            <div className="checkout-row">
              <label>
                First name
                <input value={form.firstName} onChange={(e) => update("firstName", e.target.value)} />
                {errors.firstName && <em>{errors.firstName}</em>}
              </label>
              <label>
                Last name
                <input value={form.lastName} onChange={(e) => update("lastName", e.target.value)} />
                {errors.lastName && <em>{errors.lastName}</em>}
              </label>
            </div>
            <label>
              Email
              <input value={form.email} onChange={(e) => update("email", e.target.value)} />
              {errors.email && <em>{errors.email}</em>}
            </label>
            <label>
              Address
              <input value={form.address} onChange={(e) => update("address", e.target.value)} />
              {errors.address && <em>{errors.address}</em>}
            </label>
            <label>
              City
              <input value={form.city} onChange={(e) => update("city", e.target.value)} />
              {errors.city && <em>{errors.city}</em>}
            </label>

            <div className="pay-head">
              <span>💳 Payment</span>
              <span className="pay-brands">VISA · MC · AMEX</span>
            </div>
            <label>
              Cardholder name
              <input
                value={form.cardName}
                onChange={(e) => update("cardName", e.target.value)}
                placeholder="Name on card"
              />
              {errors.cardName && <em>{errors.cardName}</em>}
            </label>
            <label>
              Card number
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
                Expiry
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
                CVC
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

            <div className="checkout-total">
              <span>Total</span>
              <strong>
                {formatGel(total)} <span className="gel">{GEL}</span>
              </strong>
            </div>
            {submitError && <div className="checkout-error">{submitError}</div>}
            <button type="submit" className="btn btn-gold checkout-btn" disabled={submitting}>
              {submitting ? "Placing order…" : "Place order"}
            </button>
          </form>
        </div>
      )}
    </main>
  );
}
