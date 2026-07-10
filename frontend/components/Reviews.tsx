"use client";

import { useEffect, useState } from "react";
import { api, type Review } from "@/lib/api";
import { useTranslation } from "@/lib/dictionary";

function Stars({ value }: { value: number }) {
  const full = Math.round(value);
  return (
    <span className="stars" aria-label={`${value.toFixed(1)} / 5`}>
      {[1, 2, 3, 4, 5].map((i) => (
        <span key={i} className={i <= full ? "star on" : "star"}>
          {i <= full ? "★" : "☆"}
        </span>
      ))}
    </span>
  );
}

export default function Reviews({ productId }: { productId: string }) {
  const { t } = useTranslation();
  const [reviews, setReviews] = useState<Review[]>([]);
  const [form, setForm] = useState({ authorName: "", rating: 5, comment: "" });
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [done, setDone] = useState(false);

  useEffect(() => {
    let cancelled = false;
    api
      .reviews(productId)
      .then((r) => {
        if (!cancelled) setReviews(r);
      })
      .catch(() => {});
    return () => {
      cancelled = true;
    };
  }, [productId]);

  const count = reviews.length;
  const average = count ? reviews.reduce((s, r) => s + r.rating, 0) / count : 0;

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setBusy(true);
    try {
      const created = await api.addReview(productId, {
        authorName: form.authorName,
        rating: form.rating,
        comment: form.comment.trim() || undefined,
      });
      setReviews((prev) => [created, ...prev]);
      setForm({ authorName: "", rating: 5, comment: "" });
      setDone(true);
      setTimeout(() => setDone(false), 2500);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to submit review");
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="reviews">
      <div className="reviews-head">
        <h3 className="serif">{t("reviews.title")}</h3>
        {count > 0 && (
          <div className="reviews-summary">
            <Stars value={average} />
            <span className="reviews-avg">{average.toFixed(1)}</span>
            <span className="reviews-count">{t("reviews.count", { n: count })}</span>
          </div>
        )}
      </div>

      {count === 0 ? (
        <div className="notice">{t("reviews.none")}</div>
      ) : (
        <ul className="review-list">
          {reviews.map((r) => (
            <li key={r.id} className="review-item">
              <div className="review-top">
                <span className="review-author">{r.authorName}</span>
                <Stars value={r.rating} />
                <span className="review-date">{new Date(r.createdAt).toLocaleDateString()}</span>
              </div>
              {r.comment && <p className="review-comment">{r.comment}</p>}
            </li>
          ))}
        </ul>
      )}

      <form className="review-form" onSubmit={submit}>
        <h4 className="serif">{t("reviews.write")}</h4>
        <div className="review-form-row">
          <label>
            {t("reviews.name")}
            <input
              value={form.authorName}
              onChange={(e) => setForm({ ...form, authorName: e.target.value })}
            />
          </label>
          <label>
            {t("reviews.rating")}
            <select
              value={form.rating}
              onChange={(e) => setForm({ ...form, rating: Number(e.target.value) })}
            >
              {[5, 4, 3, 2, 1].map((n) => (
                <option key={n} value={n}>
                  {"★".repeat(n)}
                </option>
              ))}
            </select>
          </label>
        </div>
        <label>
          {t("reviews.comment")}
          <textarea
            rows={3}
            value={form.comment}
            onChange={(e) => setForm({ ...form, comment: e.target.value })}
          />
        </label>
        {error && <div className="checkout-error">{error}</div>}
        {done && <div className="review-thanks">{t("reviews.thanks")}</div>}
        <button type="submit" className="btn btn-gold" disabled={busy}>
          {busy ? t("reviews.submitting") : t("reviews.submit")}
        </button>
      </form>
    </section>
  );
}
