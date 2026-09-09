# LuxShop — Full-Stack E-Commerce with a RAG Shopping Assistant

A production-shaped e-commerce platform: **Next.js 14** storefront, **Spring Boot 3 / Java 21** REST API,
**PostgreSQL + pgvector**, and a retrieval-augmented shopping assistant running on Gemini.

Built solo, shipped through pull requests, tested in CI.

![PRs](https://img.shields.io/badge/pull_requests-69_merged-c9a24b)
![Tests](https://img.shields.io/badge/tests-90_automated-16294d)
![Java](https://img.shields.io/badge/Java-21-e76f00)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.5-6db33f)
![i18n](https://img.shields.io/badge/i18n-18_languages-1e3a6b)
![CI](https://img.shields.io/badge/CI-passing-2e7d32)

📋 Planning board: **[LuxShop – Project #5](https://github.com/users/sarangisarang/projects/5/views/2)**

---

## At a glance

| | |
| --- | --- |
| **Scale** | ~9,250 lines · 102 Java files · 29 TypeScript modules · 14 Flyway migrations |
| **Process** | 69 merged pull requests, every one green in CI before merge |
| **Backend** | Java 21, Spring Boot 3.3.5, Spring Security (JWT), JPA/Hibernate, Bean Validation, springdoc/OpenAPI |
| **AI** | Spring AI 1.0.0-M3, pgvector store, Gemini embeddings + chat via the OpenAI-compatible API |
| **Frontend** | Next.js 14 (App Router), React, TypeScript, hand-written CSS design system |
| **Data** | PostgreSQL + Flyway (prod) · H2 (tests) |
| **DevOps** | Docker Compose, GitHub Actions (backend `mvnw verify` + frontend build), Render blueprint |

---

## What it does

**Storefront** — catalog with search, sort and category filters; cart and guest checkout with real
orders persisted to the database; order history by email; wishlist; reviews and star ratings;
discount coupons; related products; "recently viewed"; multi-currency prices with live FX rates;
product image galleries; order-confirmation email; UI in **18 languages** with locale-aware catalog
content (`Accept-Language`).

**Admin** — full CRUD over categories, products and orders, gallery management, and a dashboard with
revenue / order / top-rated analytics.

**AI** — a semantic search endpoint (`GET /shop/search/semantic` — *"a gift for a diver"* finds the
dive watch) and a RAG shopping assistant (`POST /shop/assistant`) that answers in the shopper's own
language and returns product cards alongside the reply.

---

## Three engineering problems worth reading about

The rest of this README is the part I would actually talk through in an interview.

### 1. Spring AI rejected Gemini's embeddings: `OpenAI Usage must not be null`

Gemini exposes an OpenAI-compatible API, so the plan was to point Spring AI's `OpenAiEmbeddingModel`
at it and move on. It failed at runtime: Spring AI's OpenAI client requires a `usage` object in the
response, and Gemini's embeddings endpoint doesn't send one. Deserialisation blew up before a single
vector was stored.

The options were to bring in a second AI provider, or to stop using the framework's client for this
one call. I chose the second: a ~70-line `EmbeddingModel` implementation that parses only the fields
Gemini actually returns.

→ [`GeminiEmbeddingModel.java`](https://github.com/sarangisarang/LuxShop/blob/main/backend/src/main/java/com/luxshop/shop/config/GeminiEmbeddingModel.java)
· wired in [`RagAiConfig.java`](https://github.com/sarangisarang/LuxShop/blob/main/backend/src/main/java/com/luxshop/shop/config/RagAiConfig.java)

Two details that mattered beyond just making it compile:

- The bean **overrides** the starter's auto-configured model rather than forking the RAG pipeline, so
  `VectorStore`, `Document` and the rest of Spring AI stay untouched.
- `gemini-embedding-001` defaults to 3072 dimensions but honours a requested size. The config reads
  the dimension from the same property as the pgvector column width, so the vector and the column can
  never silently drift apart.

The chat side is hand-rolled too —
[`GeminiChatClient`](https://github.com/sarangisarang/LuxShop/blob/main/backend/src/main/java/com/luxshop/shop/config/GeminiChatClient.java)
— to keep control of the endpoint path and the token budget. That budget is not cosmetic:
`gemini-flash-latest` is a *thinking* model that spends tokens reasoning before it emits any answer,
so with a default cap it returns an empty string and looks broken. It also returned 429/503
persistently under load, so the assistant now runs on `gemini-flash-lite-latest`, which answers
directly and stays available. Model choice turned out to be an availability decision, not a quality
one — the reasoning is written down next to the property, in
[`application-postgres.properties`](https://github.com/sarangisarang/LuxShop/blob/main/backend/src/main/resources/application-postgres.properties).

Retrieval runs on pgvector with an HNSW index and cosine distance over 768-dimension vectors. A
[startup runner](https://github.com/sarangisarang/LuxShop/blob/main/backend/src/main/java/com/luxshop/shop/config/EmbeddingBackfillRunner.java)
embeds the catalog once — only when the store is still empty, so restarts don't re-embed — and
swallows its own failures. A missing key or no network means semantic search stays empty; it never
means the application fails to boot.

### 2. Making the assistant trustworthy: grounding, relevance, and a fallback

An assistant that invents products is worse than no assistant. Three constraints, in
[`RagChatService.java`](https://github.com/sarangisarang/LuxShop/blob/main/backend/src/main/java/com/luxshop/shop/service/RagChatService.java):

**Grounding.** Vector search retrieves 6 candidates; the system prompt allows recommendations *only*
from that list, and instructs the model to say so honestly when nothing fits.

**Relevance.** Early on, the UI showed all 6 retrieved products even when the model's text only
endorsed one — the cards contradicted the answer. Now the model emits a trailing
`RECOMMENDED_IDS:` line, which the service parses off the reply and intersects with the retrieved
candidates. Only genuinely recommended products become cards, and an id the model hallucinates simply
doesn't resolve.

**Degradation.** Gemini returns 503/overload and 429/rate-limit under load. The chat client retries
three times with linear backoff; if it still fails, the service does *not* fail the request — the
vector search has already found relevant products, so it returns them with a friendly note. The
shopper gets a slightly worse experience instead of an error page.

Reply language is decoupled from UI language: the assistant answers in the language of the *question*,
so a German question on the Georgian storefront still gets a German answer.

Covered by unit tests in
[`RagChatServiceUnitTest.java`](https://github.com/sarangisarang/LuxShop/blob/main/backend/src/test/java/com/luxshop/RagChatServiceUnitTest.java)
— marker parsing, hallucinated ids, blank replies, and the outage fallback.

### 3. Order status as a state machine, not four setters

The order endpoints started as four independent "set status" calls, which meant a closed order could
be pushed back to pending, or an unprocessed one marked shipped. Status became an explicit transition
table — target status → the statuses it may legally be reached from — and an illegal transition
returns **409 Conflict** instead of being silently applied. Setting the current status again is an
idempotent no-op, so a double-clicked admin button is harmless.

→ [`OrderService.java` L74–L96](https://github.com/sarangisarang/LuxShop/blob/main/backend/src/main/java/com/luxshop/shop/service/OrderService.java#L74-L96)
· tested in [`AdminOrderStatusMockMvcTest.java`](https://github.com/sarangisarang/LuxShop/blob/main/backend/src/test/java/com/luxshop/AdminOrderStatusMockMvcTest.java)

---

## Security

Stateless JWT (`jjwt`) with BCrypt hashing, validated in a filter ahead of the username/password
filter. The rule is deny-by-default: `anyRequest().authenticated()`, with an explicit allowlist for
the public storefront surface — read-only catalog, guest checkout, guest reviews, semantic search, the
assistant endpoint, and the OpenAPI docs. Entities never cross the wire; every response goes through a
DTO, so no password hash or lazy-loaded relation can leak.

The signing secret is validated at startup: blank or under 256 bits and the application refuses to
boot. Production (the `postgres` profile) has no default for it at all, so a deploy that forgets
`JWT_SECRET` fails loudly instead of quietly signing tokens with a key that is public in this
repository.

→ [`CustomWebSecurityConfiguration.java`](https://github.com/sarangisarang/LuxShop/blob/main/backend/src/main/java/com/luxshop/shop/config/CustomWebSecurityConfiguration.java)
· [`security/`](https://github.com/sarangisarang/LuxShop/tree/main/backend/src/main/java/com/luxshop/shop/security)

---

## Architecture

```
luxshop/
├─ backend/    Spring Boot 3 · REST API under /shop
│              controller → service → repository, DTOs at the boundary
│              Postgres + Flyway + pgvector (prod) · H2 (tests)
└─ frontend/   Next.js 14 App Router (TypeScript)
               proxies /api/* → backend; SSR calls the backend directly
```

**Domain model**

```
Category ──1:N──> Product ──1:N──> Review
                     │  └──1:N──> ProductImage, ProductTranslation
                     └──N:1──> OrderDetails ──N:1──> Orders ──N:1──> Customer

Order lifecycle:  Pending ⇄ Processing → shipped → closed
Auth:             ServiceUser ──1:N──> UserRole        (JWT Bearer)
Coupon:           applied at checkout
Vector store:     product embeddings in pgvector (V13 migration)
```

The whole RAG stack sits behind `@Profile("postgres")`, so the H2 test profile never instantiates a
vector store or reaches for an API key — the 90 tests run offline, with no credentials.

---

## Run it

```bash
docker compose up -d --build
```

| Service | URL |
| --- | --- |
| Storefront | `http://localhost:3000` |
| Admin panel | `http://localhost:3000/admin` · `admin` / `1234` |
| API | `http://localhost:8080` |
| API docs | `http://localhost:8080/swagger-ui.html` |

The AI features need a Gemini API key (`GEMINI_API_KEY`); everything else runs without one.
Deployment blueprint: [`render.yaml`](render.yaml) · notes in [`DEPLOY.md`](DEPLOY.md).

## Tests

```bash
cd backend && ./mvnw test
```

90 tests across 20 classes — MockMvc integration tests for auth, checkout, the order state machine,
i18n and locale resolution, search, sorting, reviews, coupons and the admin gallery; unit tests for
order logic, translation storage, JWT secret validation and the assistant's answer assembly. Both backend and frontend build
on every pull request via [GitHub Actions](.github/workflows/ci.yml).

---

© 2026 Beka Kikalishvili. All rights reserved.
This code is published for portfolio and evaluation purposes and is **not licensed for commercial use
without written permission**.
