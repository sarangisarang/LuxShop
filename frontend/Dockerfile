# --- Build stage ---
FROM node:20-alpine AS build
WORKDIR /app
# Next bakes rewrite destinations at build time, so the backend URL must be set here.
ARG BACKEND_URL=http://backend:8080
ENV BACKEND_URL=$BACKEND_URL
COPY package.json package-lock.json* ./
RUN npm ci
COPY . .
RUN npm run build

# --- Runtime stage: Next.js standalone server ---
FROM node:20-alpine
WORKDIR /app
ENV NODE_ENV=production
# Standalone output bundles the server; static assets are copied alongside it.
COPY --from=build /app/.next/standalone ./
COPY --from=build /app/.next/static ./.next/static
EXPOSE 3000
CMD ["node", "server.js"]
