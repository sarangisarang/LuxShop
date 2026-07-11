-- Enable the pgvector extension so the database can store and search embeddings
-- for RAG (semantic product search / assistant). Requires the pgvector/pgvector
-- Postgres image; runs only under the postgres profile (H2 tests skip Flyway).
CREATE EXTENSION IF NOT EXISTS vector;
