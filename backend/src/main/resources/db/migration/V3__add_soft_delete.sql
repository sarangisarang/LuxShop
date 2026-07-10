-- #18 Soft delete: add an is_deleted flag to the entities that support logical
-- deletion. Existing rows default to false (not deleted). Hibernate maps this to
-- the `deleted` field and hides flagged rows via @Where("is_deleted = false").
ALTER TABLE product ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE category ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE orders ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;
