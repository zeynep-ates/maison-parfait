-- USERS
-- email UNIQUE olduğu için otomatik index var

-- PRODUCTS
create index if not exists idx_products_is_active
    on products(is_active);

create index if not exists idx_products_created_at
    on products(created_at);

-- PRODUCT IMAGES
create index if not exists idx_product_images_product_id
    on product_images(product_id);

create index if not exists idx_product_images_primary
    on product_images(product_id, is_primary);

-- CATEGORIES
create index if not exists idx_categories_parent_id
    on categories(parent_id);

create index if not exists idx_categories_kind
    on categories(kind);

create index if not exists idx_categories_is_active
    on categories(is_active);

create index if not exists idx_categories_sort_order
    on categories(sort_order);

-- PRODUCT_CATEGORIES (M:N)

create index if not exists idx_product_categories_category_id
    on product_categories(category_id);

-- PRODUCT VARIANTS
create index if not exists idx_product_variants_product_id
    on product_variants(product_id);

create index if not exists idx_product_variants_is_active
    on product_variants(is_active);

create index if not exists idx_product_variants_attributes
    on product_variants using gin (attributes);

-- JSONB filtreleme
create index if not exists idx_product_variants_attributes
    on product_variants using gin (attributes);

-- TAGS
create index if not exists idx_tags_type
    on tags(type);

create index if not exists idx_tags_is_active
    on tags(is_active);

create index if not exists idx_tags_sort_order
    on tags(sort_order);

-- PRODUCT_TAGS (M:N)
create index if not exists idx_product_tags_tag_id
    on product_tags(tag_id);

-- ADDRESSES
create index if not exists idx_addresses_user_id
    on addresses(user_id);

-- ORDERS
create index if not exists idx_orders_user_id
    on orders(user_id);

create index if not exists idx_orders_status
    on orders(status);

create index if not exists idx_orders_created_at
    on orders(created_at);

create index if not exists idx_orders_status_created_at
    on orders(status, created_at);

-- ORDER_ADDRESSES
create index if not exists idx_order_addresses_order_id
    on order_addresses(order_id);

-- ORDER_ITEMS
create index if not exists idx_order_items_order_id
    on order_items(order_id);

create index if not exists idx_order_items_product_id
    on order_items(product_id);

create index if not exists idx_order_items_variant_id
    on order_items(variant_id);

-- PAYMENTS
create index if not exists idx_payments_order_id
    on payments(order_id);

create index if not exists idx_payments_status
    on payments(status);

create index if not exists idx_payments_provider_payment_id
    on payments(provider_payment_id);

create index if not exists idx_payments_created_at
    on payments(created_at);