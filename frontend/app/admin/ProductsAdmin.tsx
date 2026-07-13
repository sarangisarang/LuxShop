"use client";

import { useCallback, useEffect, useState } from "react";
import { admin, api, type Category, type Product, type ProductImageItem, type ProductInput } from "@/lib/api";
import { useCurrency } from "@/lib/currency";

const EMPTY = {
  id: "",
  productName: "",
  productDesc: "",
  imageUrl: "",
  categoryId: "",
  price: "",
  stock: "",
};

export default function ProductsAdmin({ token }: { token: string }) {
  const { format } = useCurrency();
  const [products, setProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [form, setForm] = useState({ ...EMPTY });
  const [editing, setEditing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [gallery, setGallery] = useState<ProductImageItem[]>([]);
  const [newImageUrl, setNewImageUrl] = useState("");

  async function loadGallery(productId: string) {
    try {
      setGallery(await api.productImages(productId));
    } catch {
      setGallery([]);
    }
  }

  async function addImage() {
    const url = newImageUrl.trim();
    if (!url || !form.id) return;
    try {
      await admin.addProductImage(token, form.id, url);
      setNewImageUrl("");
      await loadGallery(form.id);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Failed to add image");
    }
  }

  async function removeImage(imageId: number) {
    try {
      await admin.deleteProductImage(token, form.id, imageId);
      await loadGallery(form.id);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Failed to remove image");
    }
  }

  const load = useCallback(async () => {
    try {
      setError(null);
      const [ps, cs] = await Promise.all([api.products(0, 100), api.categories()]);
      setProducts(ps);
      setCategories(cs);
      setForm((f) => (f.categoryId ? f : { ...f, categoryId: cs[0]?.id ?? "" }));
    } catch (e) {
      setError(e instanceof Error ? e.message : "Failed to load products");
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  function reset() {
    setEditing(false);
    setForm({ ...EMPTY, categoryId: categories[0]?.id ?? "" });
    setGallery([]);
    setNewImageUrl("");
  }

  function startEdit(p: Product) {
    setEditing(true);
    setError(null);
    setForm({
      id: p.id,
      productName: p.productName,
      productDesc: p.productDesc,
      imageUrl: p.imageUrl ?? "",
      categoryId: p.category?.id ?? "",
      price: String(p.price ?? ""),
      stock: String(p.stock ?? ""),
    });
    loadGallery(p.id);
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setBusy(true);
    const payload: ProductInput = {
      productName: form.productName.trim(),
      productDesc: form.productDesc.trim(),
      imageUrl: form.imageUrl.trim(),
      price: Number(form.price),
      stock: Number(form.stock),
    };
    try {
      if (editing) {
        await admin.updateProduct(token, form.id, payload);
      } else {
        await admin.createProduct(token, form.categoryId, payload);
      }
      reset();
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Save failed");
    } finally {
      setBusy(false);
    }
  }

  async function remove(p: Product) {
    if (!window.confirm(`Delete "${p.productName}"?`)) return;
    setError(null);
    try {
      await admin.deleteProduct(token, p.id);
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Delete failed");
    }
  }

  return (
    <div>
      {error && <div className="checkout-error" style={{ marginBottom: 16 }}>{error}</div>}

      <form className="review-form product-form" onSubmit={submit}>
        <h4 className="serif">{editing ? "Edit product" : "Add product"}</h4>
        <label>
          Name
          <input
            value={form.productName}
            onChange={(e) => setForm({ ...form, productName: e.target.value })}
          />
        </label>
        <label>
          Description
          <textarea
            rows={2}
            value={form.productDesc}
            onChange={(e) => setForm({ ...form, productDesc: e.target.value })}
          />
        </label>
        <label>
          Image URL
          <input value={form.imageUrl} onChange={(e) => setForm({ ...form, imageUrl: e.target.value })} />
        </label>
        <div className="review-form-row">
          <label>
            Category
            <select
              value={form.categoryId}
              disabled={editing}
              onChange={(e) => setForm({ ...form, categoryId: e.target.value })}
            >
              {categories.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </label>
          <label>
            Price (₾)
            <input
              type="number"
              step="0.01"
              value={form.price}
              onChange={(e) => setForm({ ...form, price: e.target.value })}
            />
          </label>
          <label>
            Stock
            <input
              type="number"
              value={form.stock}
              onChange={(e) => setForm({ ...form, stock: e.target.value })}
            />
          </label>
        </div>

        {editing && (
          <div className="gallery-admin">
            <label>Gallery images</label>
            <div className="gallery-admin-list">
              {gallery.map((img) => (
                <div className="gallery-admin-item" key={img.id}>
                  {/* eslint-disable-next-line @next/next/no-img-element */}
                  <img
                    src={img.url}
                    alt=""
                    onError={(e) => {
                      (e.currentTarget as HTMLImageElement).src = `https://picsum.photos/seed/g${img.id}/80/80`;
                    }}
                  />
                  <button
                    type="button"
                    className="gallery-admin-del"
                    title="Remove image"
                    onClick={() => removeImage(img.id)}
                  >
                    ✕
                  </button>
                </div>
              ))}
              {gallery.length === 0 && <span className="section-sub">No extra images.</span>}
            </div>
            <div className="gallery-admin-add">
              <input
                value={newImageUrl}
                onChange={(e) => setNewImageUrl(e.target.value)}
                placeholder="https://image-url…"
              />
              <button type="button" className="btn btn-navy admin-btn" onClick={addImage}>
                Add image
              </button>
            </div>
          </div>
        )}

        <div className="admin-actions">
          <button type="submit" className="btn btn-gold" disabled={busy}>
            {busy ? "Saving…" : editing ? "Save changes" : "Add product"}
          </button>
          {editing && (
            <button type="button" className="btn btn-ghost" onClick={reset}>
              Cancel
            </button>
          )}
        </div>
      </form>

      <div className="admin-product-list">
        {products.map((p) => (
          <div className="admin-product-row" key={p.id}>
            <img
              className="admin-product-thumb"
              src={p.imageUrl ?? ""}
              alt={p.productName}
              onError={(e) => {
                (e.currentTarget as HTMLImageElement).src = `https://picsum.photos/seed/${p.id}/80/80`;
              }}
            />
            <div className="admin-product-info">
              <span className="admin-product-name">{p.productName}</span>
              <span className="section-sub">
                {p.category?.name ?? "—"} · {format(Number(p.price ?? 0))} · stock {p.stock}
              </span>
            </div>
            <div className="admin-actions">
              <button className="btn btn-navy admin-btn" onClick={() => startEdit(p)}>
                Edit
              </button>
              <button className="btn btn-ghost admin-btn" onClick={() => remove(p)}>
                Delete
              </button>
            </div>
          </div>
        ))}
        {products.length === 0 && <div className="notice">No products.</div>}
      </div>
    </div>
  );
}
