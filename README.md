# 🛍️ LuxShop — Full-Stack E-commerce Platform

სრულფასოვანი, **Production-Ready** full-stack e-commerce პლატფორმა: **Next.js** storefront,
**Spring Boot 3** REST API და **PostgreSQL**. აგებულია JWT-ავტორიზაციით, i18n-ით (18 ენა),
Docker-ით და GitHub Actions CI/CD-ით.

![PRs](https://img.shields.io/badge/PRs-17_merged-c9a24b)
![Tests](https://img.shields.io/badge/tests-70%2B-16294d)
![i18n](https://img.shields.io/badge/i18n-18_languages-1e3a6b)
![CI](https://img.shields.io/badge/CI-green-2e7d32)

📋 დაგეგმვა: **[LuxShop – Project #5](https://github.com/users/sarangisarang/projects/5/views/2)**

---

## 🚀 მთავარი ფიჩერები

### 🛒 Storefront (Next.js)
* **სრული Shopping Flow:** Cart (localStorage), Checkout ფორმა ბარათის ველებით, რეალური Order-ები DB-ში და Order history.
* **ჭკვიანი კატალოგი:** პროდუქტების ძებნა, სორტირება (ფასი / სახელი / **რეიტინგი**), category ფილტრი.
* **მომხმარებლის გამოცდილება:** Wishlist (❤), **Reviews & Ratings** სისტემა (ვარსკვლავები პროდუქტ ბარათებზეც).
* **მარკეტინგი:** ფასდაკლების კუპონები (`WELCOME10` / `LUX20`), "თქვენ შესაძლოა მოგეწონოთ" (Related products), Homepage **Top-Rated** showcase.

### 🛡️ Backend & Admin (Spring Boot 3)
* **უსაფრთხოება:** **JWT**-ზე დაფუძნებული ავტორიზაცია, BCrypt, Bean Validation, DTO ფენა (0% Entity/Password გაჟონვა).
* **ბიზნეს ლოგიკა:** მკაცრი Order Lifecycle (**State Machine**), Soft Delete მექანიზმი, Pagination + Sorting.
* **მონაცემთა ბაზა:** PostgreSQL + **Flyway** მიგრაციები (V1–V12), მონაცემთა ტიპების უნიფიკაცია (`BigDecimal` ფულისთვის).
* **მრავალენოვნება:** i18n მხარდაჭერა (**18 ენა**) locale-aware კატალოგით (`Accept-Language`).
* **ხარისხი:** **70+** ავტომატიზებული ტესტი (MockMvc + Unit).
* **ადმინ პანელი:** სრული CRUD (Categories · Products · Orders), Dashboard ანალიტიკით (Revenue / Orders / Top-rated).

---

## 🛠 ტექნოლოგიური სტეკი

* **Frontend:** Next.js 14 (App Router), React, TypeScript, custom CSS design system
* **Backend:** Java 17 / 21, Spring Boot 3, Spring Security (JWT)
* **Database:** PostgreSQL + Flyway  ·  H2 (tests)
* **DevOps:** Docker, Docker Compose, GitHub Actions (CI/CD), Render blueprint

---

## 🏗️ არქიტექტურა

```
luxshop/
├─ backend/     Spring Boot 3 + JPA/Hibernate + Spring Security (Java 17/21, Maven)
│              REST API under /shop · Postgres (Flyway) in prod, H2 (data.sql) in tests
└─ frontend/    Next.js 14 (App Router, TypeScript) storefront
               proxies /api/* → backend; server-side rendering hits the backend directly
```

### Domain model

```
Category ──1:N──> Product ──N:1──┐
                                 ├──> OrderDetails ──N:1──> Orders ──N:1──> Customer
Product ──1:N──> Review          │   Product ──1:N──> ProductTranslation
                                 │   Coupon (checkout discount)
Order lifecycle: Pending → Processing → shipped → closed
Auth: ServiceUser ──1:N──> UserRole   (JWT Bearer)
```

---

## 🏃 როგორ გავუშვათ პროექტი (Quickstart)

პროექტი კონფიგურირებულია Docker Compose-ით — ერთი ბრძანება აწყობს Postgres-ს, backend-სა და frontend-ს.

1. დარწმუნდი, რომ დაინსტალირებული გაქვს **Docker** და **Docker Compose**.
2. გადადი პროექტის ფესვში და გაუშვი:

   ```bash
   docker compose up -d --build
   ```

3. სერვისები ხელმისაწვდომია:

   | სერვისი | მისამართი |
   | --- | --- |
   | 🛍️ Storefront | `http://localhost:3000` |
   | 🔐 Admin Panel | `http://localhost:3000/admin` · `admin` / `1234` |
   | 🔌 API Backend | `http://localhost:8080` |

---

## ✅ ტესტირება

```bash
cd backend && ./mvnw test
```

70+ ტესტი (MockMvc + Unit) — auth, checkout, order state-machine, i18n, search / sort / reviews / coupons — ყოველ PR-ზე CI-ში 🟢.

---

## 🗺️ Roadmap & Next Steps

* 🤖 **RAG / AI ასისტენტი** — pgvector + Spring AI ინტეგრაცია ჭკვიანი სემანტიკური ძებნისა და კონსულტანტისთვის.
* ☁️ **Cloud Deployment** — რეალურ **Render** ღრუბელში ატვირთვა (blueprint მზადაა: [`render.yaml`](render.yaml)).
* 📧 **Order confirmation email** (SMTP / logging fallback).
* 👤 **Customer accounts** — storefront login / "My account".
* 🖼️ **Product image gallery** — რამდენიმე სურათი პროდუქტზე.
