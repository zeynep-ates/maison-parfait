-- 1) Duplicate index fix
drop index if exists idx_product_variants_attributes;

create index if not exists idx_product_variants_attributes
    on product_variants using gin (attributes);

-- 2) Ensure one primary image per product (safe cleanup + unique index)
with ranked as (
    select id,
           product_id,
           row_number() over (partition by product_id order by id) as rn
    from product_images
    where is_primary = true
)
update product_images pi
set is_primary = false
    from ranked r
where pi.id = r.id
  and r.rn > 1;

create unique index if not exists ux_product_images_one_primary
    on product_images(product_id)
    where is_primary = true;
