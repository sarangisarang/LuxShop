// Thin API client for the LuxShop backend.
// In dev, calls go through Next's /api rewrite to the Spring Boot server.

const BASE = process.env.NEXT_PUBLIC_API_BASE ?? "/api";

export interface Product {
  id: string;
  productName: string;
  productDesc: string;
  imageUrl?: string | null;
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

// Spring Data Page envelope: the list endpoints return this instead of a bare array.
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number; // zero-based current page index
  size: number;
  first: boolean;
  last: boolean;
}

async function get<T>(path: string): Promise<T> {
  const res = await fetch(`${BASE}${path}`, { cache: "no-store" });
  if (!res.ok) throw new Error(`API ${path} failed: ${res.status}`);
  return res.json() as Promise<T>;
}

export const api = {
  // The catalog list endpoints are paginated; expose both the raw page (for
  // pagination UI) and a convenience unwrap to the content array.
  productsPage: (page = 0, size = 12) =>
    get<Page<Product>>(`/shop/products?page=${page}&size=${size}`),
  products: (page = 0, size = 12) =>
    api.productsPage(page, size).then((p) => p.content),
  product: (id: string) => get<Product>(`/shop/product/${encodeURIComponent(id)}`),
  categoriesPage: (page = 0, size = 100) =>
    get<Page<Category>>(`/shop/categories?page=${page}&size=${size}`),
  categories: () => api.categoriesPage().then((p) => p.content),
};

// Demo catalog used when the backend is unreachable, so the storefront still
// renders during design work / offline preview.
export const DEMO_PRODUCTS: Product[] = [
  {
    id: "1",
    productName: "Rolex Submariner Date",
    productDesc: "Iconic 41mm Oystersteel dive watch with a Cerachrom bezel.",
    imageUrl: "https://loremflickr.com/600/400/rolex,watch?lock=1",
    price: 38500,
    stock: 3,
    category: { id: "1", name: "Watches" },
  },
  {
    id: "3",
    productName: 'MacBook Pro 16" M3 Max',
    productDesc: "36GB unified memory, 1TB SSD, Liquid Retina XDR display.",
    imageUrl: "https://loremflickr.com/600/400/macbook,laptop?lock=3",
    price: 18999,
    stock: 8,
    category: { id: "2", name: "Laptops" },
  },
  {
    id: "5",
    productName: "iPhone 15 Pro Max",
    productDesc: "Aerospace-grade titanium, A17 Pro chip, 512GB.",
    imageUrl: "https://loremflickr.com/600/400/iphone,smartphone?lock=5",
    price: 4299,
    stock: 15,
    category: { id: "3", name: "Smartphones" },
  },
  {
    id: "7",
    productName: "Sony WH-1000XM5",
    productDesc: "Industry-leading noise-cancelling over-ear headphones.",
    imageUrl: "https://loremflickr.com/600/400/headphones?lock=7",
    price: 1099,
    stock: 20,
    category: { id: "4", name: "Audio" },
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
