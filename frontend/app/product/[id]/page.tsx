import Link from "next/link";
import ProductDetail from "@/components/ProductDetail";
import { loadProduct } from "@/lib/api";

export default async function ProductPage({ params }: { params: { id: string } }) {
  const { product, live } = await loadProduct(params.id);

  if (!product) {
    return (
      <main className="container section">
        <div className="notice">
          Product not found. <Link href="/">Back to store</Link>.
        </div>
      </main>
    );
  }

  return (
    <main>
      {!live && (
        <div className="container" style={{ paddingTop: 16 }}>
          <div className="notice">Demo product — the backend API is not reachable.</div>
        </div>
      )}
      <ProductDetail product={product} />
    </main>
  );
}
