-- Seed data for PostgreSQL (mirrors the H2 data.sql used by tests).
-- IDs are quoted (varchar columns), product images are left NULL, and the admin
-- password is a BCrypt hash of "1234".

INSERT INTO category (id, name, image, description) VALUES
    ('1', 'book', 'literatura', 'beletresica'),
    ('2', 'Computer', ' ', 'Computer'),
    ('3', 'Phone', ' ', 'SmartPhone');

INSERT INTO customer (id, email, first_name, last_name, password, address, postcode, city, phone) VALUES
    ('1', 'bekakikalishvili@gmail.com', 'lilian', 'mircos', 'dushqu', 'birkestrase50', 40233, 'dusseldorf', 15434232),
    ('2', 'quli2007@mail.ru', 'Sofio', 'abuladze', 'Secreto', 'birkestrase50', 40233, 'dusseldorf', 1543423232),
    ('3', 'quli2007@mail.ru', 'björn', 'seiffert', 'Secreto', 'birkestrase178', 42344, 'witten', 127777777);

INSERT INTO product (id, product_name, product_desc, prece, stock, category_id) VALUES
    ('1', 'Book', 'Dostoyevsky', 40, 200, '1'),
    ('2', 'Apple', 'MacBook16', 2000, 100, '3'),
    ('3', 'Apple', 'MacBook13', 1000, 100, '2');

INSERT INTO orders (id, order_no, order_date, order_total, shipping_date, is_delivered, customer_id, order_status) VALUES
    ('1', 10, '2015-10-11', 30, '2013-02-02', 'Waiting', '1', 'Pending');

INSERT INTO order_details (id, qty, price, subtotal, order_id, product_id) VALUES
    ('1', 1, 20, 20, '1', '1'),
    ('2', 2, 2000, 4000, '1', '2'),
    ('3', 2, 1000, 2000, '1', '3');

-- Password is the BCrypt hash of "1234".
INSERT INTO service_user (id, username, password) VALUES
    ('1', 'admin', '$2a$10$Req4kJOSY0g8pYA352BrUeWzkn6PGJh4jZs3t0W6SR0ujFIt16Y7K');

INSERT INTO user_role (id, role_name, user_id) VALUES
    ('1', 'ROLE_ADMIN', '1');
