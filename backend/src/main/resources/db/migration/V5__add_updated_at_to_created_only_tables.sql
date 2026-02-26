-- Add updated_at columns to tables that previously only had created_at

alter table product_images
    add column if not exists updated_at timestamptz not null default now();

alter table tags
    add column if not exists updated_at timestamptz not null default now();

alter table order_addresses
    add column if not exists updated_at timestamptz not null default now();

alter table order_items
    add column if not exists updated_at timestamptz not null default now();