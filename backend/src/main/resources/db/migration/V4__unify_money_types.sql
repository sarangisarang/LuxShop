-- #13 Type unification: money is BigDecimal (numeric with 2 decimals), quantity
-- is a plain integer. Align the schema so Hibernate's ddl-auto=validate passes.
-- Money columns -> numeric(38, 2):
ALTER TABLE product       ALTER COLUMN price     TYPE numeric(38, 2);
ALTER TABLE orders        ALTER COLUMN order_total TYPE numeric(38, 2);
ALTER TABLE order_details ALTER COLUMN price     TYPE numeric(38, 2);
ALTER TABLE order_details ALTER COLUMN subtotal  TYPE numeric(38, 2);
-- Quantity column -> integer (was numeric to match the old BigInteger stock):
ALTER TABLE product       ALTER COLUMN stock     TYPE integer USING stock::integer;
