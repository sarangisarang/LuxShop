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
  averageRating?: number | null;
  reviewCount?: number;
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

// Send the chosen UI language (client-side) so the backend localizes responses.
function langHeaders(): Record<string, string> {
  if (typeof window !== "undefined") {
    const lang = localStorage.getItem("luxshop_lang");
    if (lang) return { "Accept-Language": lang };
  }
  return {};
}

async function get<T>(path: string): Promise<T> {
  const res = await fetch(`${BASE}${path}`, { cache: "no-store", headers: langHeaders() });
  if (!res.ok) throw new Error(`API ${path} failed: ${res.status}`);
  return res.json() as Promise<T>;
}

async function post<T>(path: string, body: unknown): Promise<T> {
  const res = await fetch(`${BASE}${path}`, {
    method: "POST",
    headers: { "Content-Type": "application/json", ...langHeaders() },
    body: JSON.stringify(body),
  });
  if (!res.ok) {
    let message = `Request failed (${res.status})`;
    try {
      const j = await res.json();
      message = j.message || message;
    } catch {
      /* non-JSON error */
    }
    throw new Error(message);
  }
  return res.json() as Promise<T>;
}

export interface CheckoutItem {
  productId: string;
  qty: number;
}
export interface CheckoutPayload {
  firstName: string;
  lastName: string;
  email: string;
  address: string;
  city: string;
  items: CheckoutItem[];
}
export interface OrderResult {
  id: string;
  orderNo: number;
  orderTotal: number;
  orderStatus: string;
}

export interface OrderLine {
  productName: string;
  qty: number;
  price: number;
  subtotal: number;
}
export interface Order {
  id: string;
  orderNo: number;
  orderDate: string;
  orderTotal: number;
  orderStatus: string;
  isDelivered: boolean;
  customer?: { email?: string; firstName?: string; lastName?: string } | null;
  details: OrderLine[];
}

export interface Review {
  id: number;
  authorName: string;
  rating: number;
  comment?: string | null;
  createdAt: string;
}
export interface NewReview {
  authorName: string;
  rating: number;
  comment?: string;
}

export const api = {
  // The catalog list endpoints are paginated; expose both the raw page (for
  // pagination UI) and a convenience unwrap to the content array.
  productsPage: (page = 0, size = 12, q?: string, sort?: string) =>
    get<Page<Product>>(
      `/shop/products?page=${page}&size=${size}` +
        (q && q.trim() ? `&q=${encodeURIComponent(q.trim())}` : "") +
        (sort ? `&sort=${encodeURIComponent(sort)}` : "")
    ),
  products: (page = 0, size = 12, q?: string, sort?: string) =>
    api.productsPage(page, size, q, sort).then((p) => p.content),
  product: (id: string) => get<Product>(`/shop/product/${encodeURIComponent(id)}`),
  related: (id: string) => get<Product[]>(`/shop/product/${encodeURIComponent(id)}/related`),
  categoriesPage: (page = 0, size = 100) =>
    get<Page<Category>>(`/shop/categories?page=${page}&size=${size}`),
  categories: () => api.categoriesPage().then((p) => p.content),
  checkout: (payload: CheckoutPayload) => post<OrderResult>("/shop/checkout", payload),
  ordersByEmail: (email: string) => get<Order[]>(`/shop/orders?email=${encodeURIComponent(email)}`),
  reviews: (productId: string) =>
    get<Review[]>(`/shop/product/${encodeURIComponent(productId)}/reviews`),
  addReview: (productId: string, payload: NewReview) =>
    post<Review>(`/shop/product/${encodeURIComponent(productId)}/reviews`, payload),
};

// Authenticated requests (Bearer token) for the admin area.
async function authGet<T>(path: string, token: string): Promise<T> {
  const res = await fetch(`${BASE}${path}`, {
    cache: "no-store",
    headers: { Authorization: `Bearer ${token}`, ...langHeaders() },
  });
  if (!res.ok) throw new Error(`API ${path} failed: ${res.status}`);
  return res.json() as Promise<T>;
}

async function authPut<T>(path: string, token: string): Promise<T> {
  const res = await fetch(`${BASE}${path}`, {
    method: "PUT",
    headers: { Authorization: `Bearer ${token}`, ...langHeaders() },
  });
  if (!res.ok) {
    let message = `Request failed (${res.status})`;
    try {
      const j = await res.json();
      message = j.message || message;
    } catch {
      /* non-JSON */
    }
    throw new Error(message);
  }
  return res.json() as Promise<T>;
}

export type OrderAction = "process" | "ship" | "close" | "pending";

export const admin = {
  orders: (token: string) => authGet<Order[]>("/shop/order", token),
  setStatus: (token: string, id: string, action: OrderAction) =>
    authPut<Order>(`/shop/order/${id}/${action}`, token),
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
