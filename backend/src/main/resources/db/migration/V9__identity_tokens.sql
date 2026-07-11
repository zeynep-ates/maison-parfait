create table refresh_tokens
(
    id                     bigserial primary key,
    version                bigint      not null default 0,
    user_id                bigint      not null references users (id) on delete cascade,
    token_hash             varchar(64) not null unique,
    expires_at             timestamptz not null,
    revoked_at             timestamptz,
    replaced_by_token_hash varchar(64),
    user_agent             varchar(255),
    ip_address             varchar(64),
    last_used_at           timestamptz,
    created_at             timestamptz not null default now(),
    updated_at             timestamptz not null default now(),
    created_by             bigint references users (id),
    updated_by             bigint references users (id)
);

create index idx_refresh_tokens_user_id on refresh_tokens (user_id);
create index idx_refresh_tokens_expires_at on refresh_tokens (expires_at);

create table verification_tokens
(
    id         bigserial primary key,
    version    bigint      not null default 0,
    user_id    bigint      not null references users (id) on delete cascade,
    type       varchar(30) not null
        check (type in ('EMAIL_VERIFY', 'PASSWORD_RESET', 'EMAIL_CHANGE')),
    token_hash varchar(64) not null unique,
    expires_at timestamptz not null,
    used_at    timestamptz,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    created_by bigint references users (id),
    updated_by bigint references users (id)
);

create index idx_verification_tokens_user_id_type on verification_tokens (user_id, type);
