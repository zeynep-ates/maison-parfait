-- Identity module: extend the existing users table rather than replace it.
-- orders/addresses/payments still hold live FKs into this table and are not
-- being rewritten in this phase - see docs/identity-module-design.md #11.

alter table users
    add column if not exists version bigint not null default 0,
    add column if not exists full_name varchar(150),
    add column if not exists enabled boolean not null default true,
    add column if not exists locked_at timestamptz,
    add column if not exists email_verified_at timestamptz,
    add column if not exists pending_email varchar(255),
    add column if not exists deleted_at timestamptz,
    add column if not exists created_by bigint references users (id),
    add column if not exists updated_by bigint references users (id);

alter table users
    alter column password_hash drop not null;

-- Replace the plain unique constraint on email with a partial one that
-- excludes soft-deleted rows, so a deleted account's email can be reused
-- by a fresh registration instead of being squatted forever.
alter table users
    drop constraint if exists users_email_key;

create unique index if not exists ux_users_email_active
    on users (email)
    where deleted_at is null;
