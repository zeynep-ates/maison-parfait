-- Dev-only seed account. Password: "ChangeMe123!" (never used for real credentials).
INSERT INTO users (email, password_hash, role, created_at, updated_at)
VALUES ('admin@maisonparfait.com',
        '$2a$10$GNn1NSlIfX6oFY9yILwkauIhitp0LiKChByedSNWNI38fg30nBaj6',
        'ADMIN',
        now(),
        now());

INSERT INTO tags (type, name, slug, sort_order, is_active, created_at, updated_at)
VALUES ('BADGE', 'Signature', 'signature', 1, true, now(), now()),
       ('BADGE', 'Bestseller', 'bestseller', 2, true, now(), now()),
       ('BADGE', 'New', 'new', 3, true, now(), now()),
       ('BADGE', 'Limited', 'limited', 4, true, now(), now()),
       ('BADGE', 'Seasonal', 'seasonal', 5, true, now(), now()),
       ('BADGE', 'Gift', 'gift', 6, true, now(), now()),

       ('DIETARY', 'Vegan', 'vegan', 1, true, now(), now()),
       ('DIETARY', 'Gluten-Free', 'gluten-free', 2, true, now(), now()),
       ('DIETARY', 'Alcohol-Free', 'alcohol-free', 3, true, now(), now()),
       ('ALLERGEN', 'Contains Nuts', 'contains-nuts', 4, true, now(), now());

INSERT INTO categories (name, slug, parent_id, kind, sort_order, is_active, created_at, updated_at)
VALUES ('Cakes', 'cakes', NULL, 'PRODUCT', 10, true, now(), now()),
       ('Tarts', 'tarts', NULL, 'PRODUCT', 20, true, now(), now()),
       ('Éclairs', 'eclairs', NULL, 'PRODUCT', 30, true, now(), now()),
       ('Macarons', 'macarons', NULL, 'PRODUCT', 40, true, now(), now()),
       ('Viennoiserie', 'viennoiserie', NULL, 'PRODUCT', 50, true, now(), now()),
       ('Chocolaterie', 'chocolaterie', NULL, 'PRODUCT', 60, true, now(), now()),
       ('Gift Boxes', 'gift-boxes', NULL, 'PRODUCT', 70, true, now(), now()),
       ('Seasonal Collection', 'seasonal-collection', NULL, 'PRODUCT', 80, true, now(), now());