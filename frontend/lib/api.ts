// Thin API client for the LuxShop backend.
// In dev, calls go through Next's /api rewrite to the Spring Boot server.

const BASE = process.env.NEXT_PUBLIC_API_BASE ?? "/api";

export interface Product {
  id: string;
  productName: string;
  productDesc: string;
  price: number;
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
  if (!res.ok) throw new Error(`API ${path} failed: ${res.status}`);
  return res.json() as Promise<T>;
}

export const api = {
  products: () => get<Product[]>("/shop/products"),
  product: (id: string) => get<Product>(`/shop/product/${encodeURIComponent(id)}`),
  categories: () => get<Category[]>("/shop/categories"),
};

// Demo catalog used when the backend is unreachable, so the storefront still
// renders during design work / offline preview.
export const DEMO_PRODUCTS: Product[] = [
  {
    id: "1",
    productName: "MacBook Pro 16-inch",
    productDesc: "M2 Max, 32GB RAM, 1TB SSD",
    price: 6799,
    stock: 2,
    category: { id: "2", name: "Computer" },
  },
  {
    id: "2",
    productName: "iPhone 15 Pro",
    productDesc: "Titanium, 256GB, A17 Pro",
    price: 3299,
    stock: 14,
    category: { id: "3", name: "Phone" },
  },
  {
    id: "3",
    productName: "The Brothers Karamazov",
    productDesc: "Fyodor Dostoevsky — collector's edition",
    price: 40,
    stock: 120,
    category: { id: "1", name: "Book" },
  },
  {
    id: "4",
    productName: "MacBook Air 13-inch",
    productDesc: "M3, 16GB RAM, 512GB SSD",
    price: 2100,
    stock: 0,
    category: { id: "2", name: "Computer" },
  },
];

export async function loadProducts(): Promise<{ products: Product[]; live: boolean }> {
  try {
    const products = await api.products();
    return { products, live: true };
  } catch {
    return { products: DEMO_PRODUCTS, live: false };
  }
}

export async function loadProduct(id: string): Promise<{ product: Product | null; live: boolean }> {
  try {
    return { product: await api.product(id), live: true };
  } catch {
    return { product: DEMO_PRODUCTS.find((p) => p.id === id) ?? null, live: false };
  }
}
