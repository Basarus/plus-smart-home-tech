create schema if not exists shopping_store;

create table if not exists shopping_store.products (
    product_id uuid primary key,
    product_name varchar(255) not null,
    description text not null,
    image_src varchar(1024) not null,
    quantity_state varchar(50) not null,
    product_state varchar(50) not null,
    product_category varchar(50) not null,
    price numeric(19, 2) not null
);