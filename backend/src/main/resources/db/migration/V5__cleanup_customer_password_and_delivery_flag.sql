-- Tech-debt cleanup.
-- The login credential lives only on service_user, so the password column on
-- customer was redundant (and stored a second copy of the hash). Drop it.
ALTER TABLE customer DROP COLUMN password;

-- Delivery is a yes/no flag, not free text. Convert the varchar column to a real
-- boolean; any existing non-"true" value (e.g. the 'Waiting' seed) becomes false.
ALTER TABLE orders ALTER COLUMN is_delivered TYPE boolean USING (is_delivered = 'true');
