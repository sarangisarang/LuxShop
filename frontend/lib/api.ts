// Thin API client for the LuxShop backend.
// In dev, calls go through Next's /api rewrite to the Spring Boot server.

const BASE = process.env.NEXT_PUBLIC_API_BASE ?? "/api";

export interface Product {
  id: string;
  productName: string;
  productDesc: string;
  prece: number; // TODO: rename to `price` once backend typo is fixed (Project #5)
  stock: number;
  category?: Category | null;
}

export interface Category {
  id: string;
  name: string;
  description?: string;
  image?: string;
}

async function get<T>(path: string): Promise<T> {
  const res = await fetch(`${BASE}${path}`, { cache: "no-store" });
  if (!res.ok) {
    throw new Error(`API ${path} failed: ${res.status}`);
  }
  return res.json() as Promise<T>;
}

export const api = {
  products: () => get<Product[]>("/shop/products"),
  categories: () => get<Category[]>("/shop/categories"),
  productsByCategory: (name: string) =>
    get<Product[]>(`/shop/products/${encodeURIComponent(name)}`),
};
