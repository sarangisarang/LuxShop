# 🛍️ LuxShop

Full-stack e-commerce demo — a **Next.js** storefront on top of a **Spring Boot** REST API.

This repository is the clean, complete rebuild of the earlier `marketing` prototype: same
business domain (catalog, orders, customers), but with a real frontend and the backend
business-logic bugs tracked and fixed one by one.

📋 Work is planned on the project board: **[LuxShop – Project #5](https://github.com/users/sarangisarang/projects/5/views/2)**

---

## 🏗️ Architecture

```
luxshop/
├─ backend/     Spring Boot 3 + JPA/Hibernate + Spring Security (Java 17, Maven)
│              REST API under /shop, in-memory H2 (seeded from data.sql)
└─ frontend/    Next.js 14 (App Router, TypeScript) storefront
               proxies /api/* → backend during dev
```

### Domain model

```
Category ──1:N──> Product ──N:1──┐
                                 ├──> OrderDetails ──N:1──> Orders ──N:1──> Customer
                                 │
Order lifecycle: Pending → Processing → shipped → closed
Auth: ServiceUser ──1:N──> UserRole   (HTTP Basic)
```

---

## 🚀 Getting started

### Backend (port 8080)

```bash
cd backend
./mvnw spring-boot:run
```

- H2 console: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:testdb`, user `sa`)
- Seeded admin: `admin` / `1234`

### Frontend (port 3000)

```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:3000. API calls to `/api/*` are proxied to the backend
(`BACKEND_URL`, default `http://localhost:8080`).

---

## 🗺️ Roadmap (see Project #5)

The backend started as a student prototype with several known issues. They are tracked
as cards and fixed incrementally:

- 🔐 **Auth & Security** — working BCrypt login, customer registration, security rules
- 📦 **Catalog** — Category/Product CRUD, stock management, safe deletes
- 🛒 **Orders** — status state-machine, correct totals, order updates
- 🧾 **Order details / cart** — subtotal & stock calculation, typed quantities
- 📊 **Reporting** — correct order-amount aggregation
- 🧹 **Quality** — consistent money types, seed fixes, typo cleanup, global error handling

Frontend features (catalog browsing, cart, checkout, account, admin) are built on top of
each stabilized backend capability.

---

## 🧰 Tech stack

| Layer     | Tech |
|-----------|------|
| Frontend  | Next.js 14, React 18, TypeScript |
| Backend   | Spring Boot 3.0, Spring Data JPA, Spring Security, Lombok |
| Database  | H2 (dev) · PostgreSQL-ready |
| Build     | Maven (backend), npm (frontend) |
