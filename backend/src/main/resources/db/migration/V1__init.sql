-- USERS
create table users
(
    id            bigserial primary key,
    email         varchar(255) not null unique,
    password_hash varchar(255) not null,
    role          varchar(50)  not null default 'CUSTOMER'
        check (role in ('CUSTOMER', 'ADMIN')),
    created_at    timestamptz  not null default now(),
    updated_at    timestamptz  not null default now()
);

-- PRODUCTS
create table products
(
    id              bigserial primary key,
    sku             varchar(64)  not null unique,
    name            varchar(255) not null,
    description     text,
    price_cents     bigint       not null,
    currency        varchar(3)   not null default 'TRY',
    stock           int          not null default 0 check (stock >= 0),
    is_active       boolean      not null default true,
    is_perishable   boolean      not null default true,
    shelf_life_days int check (shelf_life_days is null or shelf_life_days >= 0),
    weight_grams    int check (weight_grams is null or weight_grams >= 0),
    created_at      timestamptz  not null default now(),
    updated_at      timestamptz  not null default now()
);

-- PRODUCT IMAGES
create table product_images
(
    id         bigserial primary key,
    product_id bigint      not null references products (id) on delete cascade,
    url        text        not null,
    alt_text   varchar(255),
    sort_order int         not null default 0,
    is_primary boolean     not null default false,
    created_at timestamptz not null default now()
);

-- CATEGORIES
create table categories
(
    id         bigserial primary key,
    name       varchar(150) not null,
    slug       varchar(160) not null unique,
    parent_id  bigint       references categories (id) on delete set null,
    kind       varchar(30)  not null default 'PRODUCT'
        check (kind in ('PRODUCT', 'BEVERAGE', 'COLLECTION')),
    sort_order int          not null default 0,
    is_active  boolean      not null default true,
    created_at timestamptz  not null default now(),
    updated_at timestamptz  not null default now()
);

-- Many-to-many: products <-> categories
create table product_categories
(
    product_id  bigint not null references products (id) on delete cascade,
    category_id bigint not null references categories (id) on delete cascade,
    primary key (product_id, category_id)
);

-- PRODUCT VARIANTS
create table product_variants
(
    id          bigserial primary key,
    product_id  bigint       not null references products (id) on delete cascade,
    sku         varchar(64)  not null unique,
    name        varchar(150) not null,
    price_cents bigint       not null,
    currency    varchar(3)   not null default 'TRY',
    stock       int          not null default 0 check (stock >= 0),
    is_active   boolean      not null default true,
    attributes  jsonb        not null default '{}'::jsonb,
    created_at  timestamptz  not null default now(),
    updated_at  timestamptz  not null default now()
);

-- TAGS
create table tags
(
    id         bigserial primary key,
    type       varchar(50)  not null
        check (type in ('ALLERGEN', 'DIETARY', 'FLAVOR', 'BADGE')),
    name       varchar(120) not null,
    slug       varchar(140) not null unique,
    sort_order int not null default 0,
    is_active  boolean not null default true,
    created_at timestamptz  not null default now()
);

create table product_tags
(
    product_id bigint not null references products (id) on delete cascade,
    tag_id     bigint not null references tags (id) on delete cascade,
    primary key (product_id, tag_id)
);

-- ADDRESSES (shipping)
create table addresses
(
    id           bigserial primary key,
    user_id      bigint       not null references users (id),
    title        varchar(100),
    full_name    varchar(120),
    phone        varchar(40),
    country      varchar(100) not null,
    city         varchar(100) not null,
    district     varchar(100),
    address_line varchar(500) not null,
    postal_code  varchar(20),
    created_at   timestamptz  not null default now(),
    updated_at   timestamptz  not null default now()
);

-- ORDERS
create table orders
(
    id                  bigserial primary key,
    user_id             bigint      not null references users (id),
    shipping_address_id bigint      not null references addresses (id),
    status              varchar(50) not null
        check (status in (
                          'CREATED',
                          'PAYMENT_PENDING',
                          'PAID',
                          'PREPARING',
                          'SHIPPED',
                          'DELIVERED',
                          'CANCELLED',
                          'REFUND_REQUESTED',
                          'REFUNDED'
            )),
    currency            varchar(3)  not null default 'TRY',
    subtotal_cents      bigint      not null default 0,
    shipping_cents      bigint      not null default 0,
    discount_cents      bigint      not null default 0,
    tax_cents           bigint      not null default 0,
    total_cents         bigint      not null default 0,
    created_at          timestamptz not null default now(),
    updated_at          timestamptz not null default now()
);

-- ORDER ADRESSES
create table order_addresses
(
    id           bigserial primary key,
    order_id     bigint       not null references orders (id) on delete cascade,
    type         varchar(20)  not null check (type in ('SHIPPING', 'BILLING')),
    full_name    varchar(120) not null,
    phone        varchar(40),
    country      varchar(100) not null,
    city         varchar(100) not null,
    district     varchar(100),
    address_line varchar(500) not null,
    postal_code  varchar(20),
    created_at   timestamptz  not null default now()
);

-- ORDER ITEMS
create table order_items
(
    id               bigserial primary key,
    order_id         bigint      not null references orders (id) on delete cascade,
    product_id       bigint      not null references products (id),
    variant_id       bigint references product_variants (id),
    quantity         int         not null,
    unit_price_cents bigint      not null,
    currency         varchar(3)  not null default 'TRY',
    created_at       timestamptz not null default now()
);

-- PAYMENTS
create table payments
(
    id                  bigserial primary key,
    order_id            bigint      not null references orders (id) on delete cascade,
    provider            varchar(50) not null,
    status              varchar(50) not null
        check (status in ('INITIATED', 'PENDING', 'SUCCEEDED', 'FAILED')),
    amount_cents        bigint         not null,
    currency            varchar(3)  not null default 'TRY',
    provider_payment_id varchar(100),
    idempotency_key     varchar(100),
    failure_reason      text,
    paid_at             timestamptz,
    created_at          timestamptz not null default now(),
    updated_at          timestamptz not null default now()
);