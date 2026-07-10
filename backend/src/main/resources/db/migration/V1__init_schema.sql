-- LuxShop initial schema for PostgreSQL.
-- Column names and types mirror what Hibernate generates for the JPA entities,
-- so `spring.jpa.hibernate.ddl-auto=validate` passes against this schema.

CREATE TABLE category (
    id          varchar(255) NOT NULL,
    name        varchar(255),
    image       varchar(255),
    description varchar(255),
    CONSTRAINT category_pkey PRIMARY KEY (id)
);

CREATE TABLE customer (
    id         varchar(255) NOT NULL,
    email      varchar(255),
    first_name varchar(255),
    last_name  varchar(255),
    password   varchar(255),
    address    varchar(255),
    postcode   integer,
    city       varchar(255),
    phone      integer,
    CONSTRAINT customer_pkey PRIMARY KEY (id)
);

CREATE TABLE service_user (
    id       varchar(255) NOT NULL,
    username varchar(255),
    password varchar(255),
    CONSTRAINT service_user_pkey PRIMARY KEY (id)
);

CREATE TABLE user_role (
    id        varchar(255) NOT NULL,
    role_name varchar(255),
    user_id   varchar(255),
    CONSTRAINT user_role_pkey PRIMARY KEY (id),
    CONSTRAINT user_role_user_fk FOREIGN KEY (user_id) REFERENCES service_user (id)
);

CREATE TABLE product (
    id           varchar(255) NOT NULL,
    product_name varchar(255),
    product_desc varchar(255),
    image1       bytea,
    image2       bytea,
    image3       bytea,
    prece        numeric(38, 0),
    stock        numeric(38, 0),
    category_id  varchar(255),
    CONSTRAINT product_pkey PRIMARY KEY (id),
    CONSTRAINT product_category_fk FOREIGN KEY (category_id) REFERENCES category (id)
);

CREATE TABLE orders (
    id            varchar(255) NOT NULL,
    order_no      integer,
    order_date    date,
    order_total   integer,
    shipping_date date,
    is_delivered  varchar(255),
    order_status  varchar(255),
    customer_id   varchar(255),
    CONSTRAINT orders_pkey PRIMARY KEY (id),
    CONSTRAINT orders_customer_fk FOREIGN KEY (customer_id) REFERENCES customer (id)
);

CREATE TABLE order_details (
    id         varchar(255) NOT NULL,
    qty        integer,
    price      integer,
    subtotal   integer,
    order_id   varchar(255),
    product_id varchar(255),
    CONSTRAINT order_details_pkey PRIMARY KEY (id),
    CONSTRAINT order_details_order_fk FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT order_details_product_fk FOREIGN KEY (product_id) REFERENCES product (id)
);
