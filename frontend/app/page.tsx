import { api, type Product } from "@/lib/api";

async function loadProducts(): Promise<Product[] | null> {
  try {
    return await api.products();
  } catch {
    // Backend not running, or the endpoint still requires auth.
    // Public product browsing is enabled as part of the Project #5 backlog.
    return null;
  }
}

export default async function Home() {
  const products = await loadProducts();

  return (
    <main>
      <section className="hero">
        <div className="container">
          <h1>
            Luxury, <span className="accent">delivered.</span>
          </h1>
          <p>
            A curated selection of books, computers and phones — the LuxShop
            demo store powered by Next.js and a Spring Boot API.
          </p>
          <a className="btn" href="#catalog">
            Browse catalog
          </a>
        </div>
      </section>

      <section id="catalog" className="container">
        <h2 className="section-title">Featured products</h2>

        {products === null ? (
          <div className="notice">
            Could not reach the backend catalog yet. Start the Spring Boot API
            (<code>cd backend &amp;&amp; ./mvnw spring-boot:run</code>) and make
            sure public product browsing is enabled. Until then, here is the
            planned layout.
          </div>
        ) : products.length === 0 ? (
          <div className="notice">No products yet — seed the database.</div>
        ) : (
          <div className="grid">
            {products.map((p) => (
              <article className="card" key={p.id}>
                <div className="cat">{p.category?.name ?? "Uncategorized"}</div>
                <h3>{p.productName}</h3>
                <div className="desc">{p.productDesc}</div>
                <div className="row">
                  <span className="price">${String(p.prece ?? "—")}</span>
                  <span className="stock">
                    {Number(p.stock) > 0 ? `${p.stock} in stock` : "Out of stock"}
                  </span>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
    </main>
  );
}
