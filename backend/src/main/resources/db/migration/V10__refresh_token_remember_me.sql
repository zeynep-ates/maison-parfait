-- Rotation needs to know whether the original token was a "remember me"
-- session so the rotated replacement gets the same TTL policy - otherwise
-- remember-me silently downgrades to the standard TTL after the first
-- refresh, defeating its purpose. See docs/identity-module-design.md.

alter table refresh_tokens
    add column if not exists remember_me boolean not null default false;
