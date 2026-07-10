# Deploying LuxShop

Two supported paths: **local Docker** (already working) and **Render** (public URL, free tier).

## Local — Docker Compose

```bash
docker compose up --build
```

- Storefront: http://localhost:3000
- API: http://localhost:8080
- Postgres: localhost:55432 (user/pass/db: `luxshop`)

Compose sets `POSTGRES_URL` directly, so the backend uses it as-is.

## Cloud — Render (one blueprint)

The repo ships a [`render.yaml`](render.yaml) blueprint that provisions:

| Resource            | What                                        |
| ------------------- | ------------------------------------------- |
| `luxshop-db`        | Managed Postgres (free plan)                |
| `luxshop-backend`   | Spring Boot API, Docker, profile `postgres` |
| `luxshop-frontend`  | Next.js storefront, Docker                  |

### Steps

1. Push this repo to GitHub (already done).
2. On https://render.com → **New → Blueprint** → select this repository. Render reads `render.yaml`.
3. Approve the plan. Render creates the database and both services. The backend
   auto-wires to the database (host/port/name/credentials are injected as env
   vars, and `application-postgres.properties` assembles the JDBC URL). `JWT_SECRET`
   is generated automatically.
4. Wait for **`luxshop-backend`** to go live, then copy its public URL
   (e.g. `https://luxshop-backend.onrender.com`).
5. Open **`luxshop-frontend`** → **Environment** → set `BACKEND_URL` to that URL →
   **Save**, then **Manual Deploy → Deploy latest commit**.
   > Next.js bakes the `/api` proxy target at build time, so the frontend must be
   > rebuilt once the backend URL is known. This is the only manual field.
6. Open the frontend URL — the storefront is live. Admin panel at `/admin`
   (demo: `admin` / `1234`).

### Notes

- **Ports** — Render assigns `$PORT`; the backend binds to it (falls back to 8080
  locally). Next.js honors `$PORT` automatically.
- **Migrations** — Flyway runs on backend startup and creates the schema + seed
  data against the fresh Postgres. `ddl-auto=validate` then checks the entities.
- **Free tier** — services sleep after inactivity (first request wakes them, ~30s)
  and the free database is time-limited; fine for a demo, upgrade plans for prod.
- **Secrets** — `JWT_SECRET` is generated per environment; never commit real
  secrets. The value in `docker-compose.yml` is a local dev placeholder only.
