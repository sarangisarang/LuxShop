"use client";

import { useCallback, useEffect, useState } from "react";
import { admin, api, type Category, type CategoryInput } from "@/lib/api";

const EMPTY = { id: "", name: "", description: "", image: "" };

export default function CategoriesAdmin({ token }: { token: string }) {
  const [categories, setCategories] = useState<Category[]>([]);
  const [form, setForm] = useState({ ...EMPTY });
  const [editing, setEditing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const load = useCallback(async () => {
    try {
      setError(null);
      setCategories(await api.categories());
    } catch (e) {
      setError(e instanceof Error ? e.message : "Failed to load categories");
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  function reset() {
    setEditing(false);
    setForm({ ...EMPTY });
  }

  function startEdit(c: Category) {
    setEditing(true);
    setError(null);
    setForm({ id: c.id, name: c.name, description: c.description ?? "", image: c.image ?? "" });
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setBusy(true);
    const payload: CategoryInput = {
      name: form.name.trim(),
      description: form.description.trim(),
      image: form.image.trim(),
    };
    try {
      if (editing) {
        await admin.updateCategory(token, form.id, payload);
      } else {
        await admin.createCategory(token, payload);
      }
      reset();
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Save failed");
    } finally {
      setBusy(false);
    }
  }

  async function remove(c: Category) {
    if (!window.confirm(`Delete "${c.name}"?`)) return;
    setError(null);
    try {
      await admin.deleteCategory(token, c.id);
      await load();
    } catch (err) {
      // The backend returns 409 when the category still has products.
      setError(
        err instanceof Error && /409/.test(err.message)
          ? `Cannot delete "${c.name}" — it still has products.`
          : err instanceof Error
            ? err.message
            : "Delete failed"
      );
    }
  }

  return (
    <div>
      {error && <div className="checkout-error" style={{ marginBottom: 16 }}>{error}</div>}

      <form className="review-form product-form" onSubmit={submit}>
        <h4 className="serif">{editing ? "Edit category" : "Add category"}</h4>
        <label>
          Name
          <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
        </label>
        <label>
          Description
          <textarea
            rows={2}
            value={form.description}
            onChange={(e) => setForm({ ...form, description: e.target.value })}
          />
        </label>
        <label>
          Image URL
          <input value={form.image} onChange={(e) => setForm({ ...form, image: e.target.value })} />
        </label>
        <div className="admin-actions">
          <button type="submit" className="btn btn-gold" disabled={busy}>
            {busy ? "Saving…" : editing ? "Save changes" : "Add category"}
          </button>
          {editing && (
            <button type="button" className="btn btn-ghost" onClick={reset}>
              Cancel
            </button>
          )}
        </div>
      </form>

      <div className="admin-product-list">
        {categories.map((c) => (
          <div className="admin-product-row" key={c.id}>
            <img
              className="admin-product-thumb"
              src={c.image ?? ""}
              alt={c.name}
              onError={(e) => {
                (e.currentTarget as HTMLImageElement).src = `https://picsum.photos/seed/${c.id}/80/80`;
              }}
            />
            <div className="admin-product-info">
              <span className="admin-product-name">{c.name}</span>
              <span className="section-sub">{c.description ?? "—"}</span>
            </div>
            <div className="admin-actions">
              <button className="btn btn-navy admin-btn" onClick={() => startEdit(c)}>
                Edit
              </button>
              <button className="btn btn-ghost admin-btn" onClick={() => remove(c)}>
                Delete
              </button>
            </div>
          </div>
        ))}
        {categories.length === 0 && <div className="notice">No categories.</div>}
      </div>
    </div>
  );
}
