import { Suspense } from "react";
import Hero from "@/components/Hero";
import Catalog from "@/components/Catalog";
import TopRated from "@/components/TopRated";
import { loadProducts } from "@/lib/api";

export default async function Home() {
  const { products, live } = await loadProducts();

  return (
    <main>
      <Hero />

      <TopRated />

      <section id="catalog" className="container section">
        {!live && (
          <div className="notice" style={{ marginBottom: 22 }}>
            Showing a demo catalog — the Spring Boot API is not reachable. Start it with{" "}
            <code>cd backend &amp;&amp; ./mvnw spring-boot:run</code> to load live products.
          </div>
        )}

        <Suspense fallback={<div className="notice">Loading catalog…</div>}>
          <Catalog products={products} />
        </Suspense>
      </section>
    </main>
  );
}
