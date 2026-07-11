# Maison Parfait — Backend Architecture (v2)

Status: **proposed, pending approval**
Scope: full backend redesign. Frontend visual design is out of scope and untouched.

## 0. Process

The current backend (`backend/src/main/java/.../modules/*`) is treated as a disposable prototype from here forward. No further incremental patching. Each module below is built fresh, tested, wired in, and only then does the corresponding legacy package get deleted. "Module by module" means: identity → catalog → inventory → cart → order/pricing → payment → shipping → review/wishlist → notification → admin. That order isn't arbitrary — it's dependency order: almost everything downstream references a user, and order/payment/shipping reference catalog+inventory, so those have to exist first.

**Migration history**: there is no production data anywhere and the current `V1`–`V6` history already contains one migration that never validly applied (fixed in the last session, but still). Rather than carrying that forward, each module's Flyway migrations will be written fresh as part of that module's phase, and once all phases are complete the full migration set is squashed to a single clean baseline before anything resembling a real deployment happens. Squashing now, before any table exists in a shared/deployed environment, has no downside — there's no applied-checksum history to protect.

**Where this document lives**: this file, updated as decisions change. Treat it as the source of truth for "why" a module looks the way it does — implementation PRs should be small enough that this doc explains more than the diff does.

---

## 1. What's kept vs. rebuilt

| Area | Verdict | Why |
|---|---|---|
| Money as `bigint` cents | **Keep** | Already correct, no reason to touch |
| `order_addresses`-style snapshotting | **Keep the pattern**, rebuilt to cover billing too | Correct e-commerce practice |
| `CategoryKind` discriminator (PRODUCT/BEVERAGE/COLLECTION) | **Keep** | Avoids a redundant collections table for no real benefit |
| Image upload (UUID filename, extension allowlist) | **Keep as-is** | Already correct, not worth rewriting |
| DTOs as Java records, Flyway, springdoc-openapi | **Keep the conventions** | No issue with these |
| `auth`, `user`, `product`, `category`, `tag`, `order`, `payment`, `admin` packages | **Delete**, rebuilt module by module | Structural issues (no refresh tokens, no ownership checks, order never attaches a user, payment confirmable by any client, stock has no reservation concept) are deep enough that patching would cost more than rebuilding |
| `docker-compose.yml` (Postgres + pgAdmin) | **Keep** | Local dev infra, not part of the backend redesign |

---

## 2. Structural approach: modular monolith + Spring Modulith

One deployable Spring Boot application — not microservices. `Spring Modulith` is added for two concrete reasons, not as decoration:

1. **It enforces the module boundaries this document defines.** `ApplicationModules.of(...).verify()` runs as a test and fails the build if, say, `payment` reaches into `order`'s internal package instead of its public API. Given the rebuild is happening exactly along these module lines, this is the safety net that keeps the boundaries real instead of aspirational.
2. **Event-driven inter-module communication** (`ApplicationEventPublisher` + `@ApplicationModuleListener`) replaces direct cross-module service calls for side effects — e.g. `OrderPaidEvent` triggers inventory deduction, a receipt email, and (later) an analytics counter, without `OrderService` knowing any of those three things exist. This matters more now than it would have on the old codebase, because there are 13 modules instead of 8, and direct service-to-service wiring at that count turns into a dependency tangle fast.

No CQRS, no event sourcing, no message broker — events here are in-process and synchronous-by-default (Modulith supports transactional-outbox-style async later if it's ever needed, but that's not being built now).

Internal layering per module stays flat and familiar: `controller → service → repository`, DTOs at the boundary, entities never leave the module. No hexagonal/ports-and-adapters ceremony — that's complexity this project doesn't need at this size.

### Module map

| Module | Owns | Replaces |
|---|---|---|
| `identity` | users, refresh tokens, email verification, password reset, sessions | `auth`, `user` |
| `catalog` | products, variants, images, categories, tags, search | `product`, `category`, `tag` |
| `inventory` | stock levels, reservations, low-stock | — (new; split out of `catalog`) |
| `cart` | guest + authenticated cart, merge-on-login | — (new) |
| `pricing` | coupons, discount calculation | — (new) |
| `order` | order lifecycle, addresses, order items | `order` (rewritten) |
| `payment` | provider abstraction, webhooks, saved payment methods | `payment` (rewritten) |
| `shipping` | shipping methods, shipments, tracking | — (new) |
| `review` | product reviews, moderation | — (new) |
| `wishlist` | saved products | — (new) |
| `notification` | email (SMS/push-ready interface) | — (new) |
| `admin` | cross-module read/aggregate endpoints, no business logic of its own | `admin` (rewritten) |
| `common` | `BaseEntity`+auditing, `CurrentUser`, exceptions, API envelope, security infra | `common` (extended) |

---

## 3. Database design by module

Money: `bigint` cents everywhere, `currency` alongside it. All primary keys `bigserial`. `created_at`/`updated_at` via `BaseEntity` on every table unless noted. `created_by`/`updated_by` (nullable FK to `users`) added via Spring Data JPA Auditing on tables where "who changed this" matters (products, orders, coupons — not on high-frequency/system-only tables like `stock_reservations`).

### identity

| Table | Key columns | Notes |
|---|---|---|
| `users` | `email` (unique), `password_hash`, `role`, `email_verified_at` (nullable) | same shape as today, `email_verified_at` is new |
| `refresh_tokens` | `user_id`, `token_hash`, `expires_at`, `revoked_at` (nullable), `user_agent`, `ip_address`, `last_used_at` | one row = one session/device; rotated on every use |
| `verification_tokens` | `user_id`, `type` (EMAIL_VERIFY/PASSWORD_RESET), `token_hash`, `expires_at`, `used_at` (nullable) | single-use, server-revocable (not JWT-based) |

### catalog

| Table | Key columns | Notes |
|---|---|---|
| `categories` | `name`, `slug`, `parent_id`, `kind`, `sort_order`, `is_active` | unchanged from today |
| `products` | `sku`, `name`, `slug` (**new**), `description`, `price_cents`, `currency`, `is_active`, `is_perishable`, `shelf_life_days`, `weight_grams`, `search_vector` (**new**, generated `tsvector`) | `stock` column removed — moves to `inventory.stock_items`; `slug` was missing before (no SEO-friendly product URLs exist today) |
| `product_images` | `product_id`, `url`, `alt_text`, `sort_order`, `is_primary` | unchanged |
| `product_variants` | `product_id`, `sku`, `name`, `price_cents`, `currency`, `attributes jsonb` | `stock` removed, same reason |
| `tags` / `product_tags` | as today | unchanged |
| `product_categories` | as today | unchanged |

GIN index on `search_vector` for full-text search; GIN on `product_variants.attributes` as today. Postgres FTS is sufficient at this scale — no Elasticsearch/Meilisearch.

### inventory

| Table | Key columns | Notes |
|---|---|---|
| `stock_items` | `product_id`, `variant_id` (nullable), `quantity_on_hand`, `low_stock_threshold` | one row per sellable unit (product or variant) |
| `stock_reservations` | `stock_item_id`, `quantity`, `cart_id` (nullable), `order_id` (nullable), `expires_at` | `available = quantity_on_hand - sum(active reservations)`; expired rows swept by a scheduled job |

### cart

| Table | Key columns | Notes |
|---|---|---|
| `carts` | `user_id` (nullable), `guest_token` (uuid, nullable), `status` (ACTIVE/MERGED/CONVERTED/ABANDONED) | exactly one of `user_id`/`guest_token` set |
| `cart_items` | `cart_id`, `product_id`, `variant_id` (nullable), `quantity` | unique on `(cart_id, product_id, variant_id)` |

### pricing

| Table | Key columns | Notes |
|---|---|---|
| `coupons` | `code` (unique), `type` (PERCENTAGE/FIXED), `value`, `min_order_cents`, `max_discount_cents` (nullable), `usage_limit`, `per_user_limit`, `valid_from`, `valid_until`, `is_active` | |
| `coupon_redemptions` | `coupon_id`, `user_id`, `order_id` | enforces `per_user_limit`, gives an audit trail |

Not a rules engine — one flat table, evaluated at checkout. A rules engine is the textbook overengineering trap for a store this size.

### order

| Table | Key columns | Notes |
|---|---|---|
| `addresses` | `user_id`, `label`, `full_name`, `phone`, `country`, `city`, `district`, `address_line`, `postal_code`, `is_default` | customer's saved address book; `label` replaces `title`, `is_default` is new |
| `orders` | `user_id`, `status`, `currency`, `subtotal_cents`, `discount_cents`, `shipping_cents`, `tax_cents`, `total_cents`, `coupon_id` (nullable), `placed_at` | **`status` now covers commercial lifecycle only** (see below) |
| `order_addresses` | `order_id`, `type` (SHIPPING/BILLING), same address fields | snapshot pattern kept, now genuinely covers both types (checkout collects both) |
| `order_items` | `order_id`, `product_id`, `variant_id` (nullable), `product_name_snapshot`, `sku_snapshot`, `quantity`, `unit_price_cents`, `currency` | name/SKU snapshots are new — today a renamed/deleted product silently rewrites history in old orders |

**`OrderStatus` split**: today one enum (`CREATED…DELIVERED…REFUNDED`) tries to model both "did we get paid" and "did the courier deliver it" at once. New design: `orders.status` = `CREATED / PAYMENT_PENDING / PAID / CANCELLED / REFUND_REQUESTED / REFUNDED` (commercial only); fulfillment state (`PENDING/LABEL_CREATED/IN_TRANSIT/DELIVERED/FAILED`) moves to `shipments.status`. This isn't a cosmetic split — it removes an implicit coupling where the order module would otherwise need to know about courier states to update its own status column.

### payment

| Table | Key columns | Notes |
|---|---|---|
| `payments` | `order_id`, `provider`, `status`, `amount_cents`, `currency`, `provider_payment_id`, `idempotency_key` (unique) | as today, `idempotency_key` now actually enforced |
| `payment_events` | `payment_id`, `provider_event_id` (unique), `type`, `raw_payload jsonb`, `received_at`, `processed_at` | **new** — webhook audit log + the mechanism that makes duplicate webhook delivery (normal with every real provider) a no-op instead of a double-charge |
| `saved_payment_methods` | `user_id`, `provider`, `provider_payment_method_id`, `brand`, `last4`, `exp_month`, `exp_year`, `is_default` | tokenized references only — raw card data never touches this database, full stop |

### shipping

| Table | Key columns | Notes |
|---|---|---|
| `shipping_methods` | `name`, `carrier`, `base_rate_cents`, `eta_days_min`, `eta_days_max`, `is_active` | admin-configured options shown at checkout |
| `shipments` | `order_id`, `shipping_method_id`, `provider`, `tracking_number`, `status`, `shipped_at`, `delivered_at` | one shipment per order for now (split shipments are a real feature but not one this store needs yet) |

### review

| Table | Key columns | Notes |
|---|---|---|
| `reviews` | `product_id`, `user_id`, `order_item_id` (unique), `rating` (1–5), `title`, `body`, `status` (PENDING/APPROVED/REJECTED), `moderated_by`, `moderated_at` | unique on `order_item_id` enforces one review per purchased item = verified-purchase reviews only |

### wishlist

| Table | Key columns | Notes |
|---|---|---|
| `wishlist_items` | `user_id`, `product_id` | unique on `(user_id, product_id)` |

### notification

| Table | Key columns | Notes |
|---|---|---|
| `notification_preferences` | `user_id` (PK), `order_updates` (bool, default true), `marketing` (bool, default false) | |

No `notification_log` table yet — logging every send isn't needed until there's a second channel or a support workflow that requires it. Add it when that need actually shows up, not preemptively.

---

## 4. Identity & security design

- **Access token**: JWT, ~15 min TTL, subject = user id (not email — email changes shouldn't invalidate tokens), claims include `role`. Held in the frontend's memory only (not localStorage), sent as `Authorization: Bearer`.
- **Refresh token**: opaque random value, stored **SHA-256 hashed** (not bcrypt — these are already high-entropy random, bcrypt's deliberate slowness only hurts revocation-check latency here) in `refresh_tokens`. Returned as an **httpOnly, Secure, SameSite=Lax cookie** scoped to `/api/auth/refresh` — never exposed to JS, which closes off the main XSS-token-theft path that plagues localStorage-based JWT storage.
- **Rotation on refresh**: every `/api/auth/refresh` call issues a new refresh token and revokes the old one. If an already-rotated (i.e. already-used) token is ever presented again, that's a replay signal — the entire session family is revoked as a precaution.
- **CSRF**: every endpoint except `/api/auth/refresh` is bearer-token-only and therefore not cookie-driven, so classic CSRF doesn't apply to them. `/api/auth/refresh` is the one cookie-based endpoint; `SameSite=Lax` is the primary defense (neutralizes cross-site POST in modern browsers), with a double-submit token as defense in depth if needed once the real frontend origin is known.
- **Sessions**: `refresh_tokens` doubles as the session table. `GET /api/users/me/sessions` lists devices (user agent, IP, last used), `DELETE .../sessions/{id}` revokes one (logout that device), `DELETE .../sessions` revokes all (logout everywhere — used after password change/reset).
- **Remember me**: not a separate mechanism, just a longer refresh-token TTL chosen at login time.
- **Email verification**: `verification_tokens` row (EMAIL_VERIFY, 24h TTL) emailed as a link. Not a hard gate on checkout for v1 — required only where it's explicitly checked later (e.g. before enabling seller-type features, if that ever exists). Blocking checkout on it is a UX cost this store doesn't need to pay yet.
- **Password reset**: `verification_tokens` row (PASSWORD_RESET, 30 min TTL, single-use via `used_at`). Successful reset revokes all refresh tokens for that user.
- **Change password** (while logged in): requires current password, revokes all *other* sessions' refresh tokens, keeps the current one.
- **RBAC**: `CUSTOMER`/`ADMIN` now, enum leaves room for `EMPLOYEE`/`DELIVERY` later — not a permissions-matrix table, that's overkill for two-to-four roles.
- **Ownership checks**: a single reusable `@Component("ownership")` bean with methods like `isOwner(Long addressId, CurrentUser user)`, used via `@PreAuthorize("@ownership.isOwner(#id, principal)")` on controller methods. One implementation, reused everywhere a resource needs to be scoped to its owner — this directly targets the IDOR class of bug that was the single biggest issue found in the legacy code.
- **CurrentUser resolution**: JWT subject (user id) + role claim resolve directly into a lightweight `CurrentUser(Long id, UserRole role)` injected into controllers via a custom argument resolver — no DB hit needed just to know who's calling; services that need the full `User` entity load it explicitly when they actually need more than id/role.

---

## 5. Payment architecture

```java
public interface PaymentProvider {
    String name(); // "mock", "stripe", "iyzico", ...
    PaymentInitiation initiate(PaymentInitiationRequest request);
    PaymentEvent parseWebhook(WebhookRequest request); // verifies signature; throws if invalid
    RefundResult refund(String providerPaymentId, long amountCents);
}
```

- Providers are Spring beans; `PaymentProviderRegistry` indexes them by `.name()` (from a `Map<String, PaymentProvider>` Spring already assembles). `PaymentService` looks up the provider recorded on the payment row — never an `if/else` chain, never a hardcoded provider anywhere in the call path.
- **Webhooks, not client confirmation**: `POST /api/webhooks/payments/{provider}` is public (no JWT — this is server-to-server) and is the *only* way a payment transitions to `SUCCEEDED`. `parseWebhook` verifies the provider's signature before anything else happens. This is the direct fix for the exploit found in the legacy code, where any logged-in customer could `POST /payments/{id}/confirm` and mark their own order paid for free.
- **Idempotency two ways**: `payment_events.provider_event_id` (unique) makes duplicate webhook delivery — which every real provider does under retry/at-least-once delivery semantics — a no-op instead of double-processing. A client-supplied `Idempotency-Key` header on `POST /api/orders/{id}/payments` (initiate) prevents a double-click/retry from creating two payment attempts.
- **`MockPaymentProvider`**: same interface, lets checkout be built and tested end-to-end before any real provider account exists. Gated by `app.payments.mock-enabled`, which must be `false` in the prod profile with no default — the app fails to start rather than silently allowing a fake payment path to exist in production.
- **Saved payment methods**: store only what the provider returns as a reference token (`provider_payment_method_id`) plus non-sensitive display metadata (brand/last4/expiry). Raw PAN/CVV never reaches this backend — that's what keeps this out of full PCI-DSS scope.

---

## 6. Shipping architecture

```java
public interface ShippingProvider {
    String name();
    List<ShippingRate> quote(ShippingQuoteRequest request); // address + cart -> rates
    ShipmentResult createShipment(Order order, ShippingMethod method); // -> tracking number
}
```

- `ManualShippingProvider` ships first: admin manually enters a tracking number and moves shipment status forward. No live carrier account exists yet, so integrating one now would be building against a hypothetical — the interface is what needs to exist today, not a working Aras/MNG/DHL integration.
- Shipment status (`PENDING → LABEL_CREATED → IN_TRANSIT → DELIVERED`, or `FAILED`) is independent of order status, per the split described in §3 — an order can be `PAID` and its shipment simultaneously `IN_TRANSIT` without either module needing to know the other's internal states, only that a `PaymentSucceededEvent` is what allows a shipment to be created in the first place.

---

## 7. Inventory architecture

Flow, end to end:

1. **Add to cart** — no reservation. A cart is not a commitment, and reserving stock for every browser tab that adds an item would starve real buyers.
2. **Checkout initiated** (order created from cart, `status = CREATED`) — reserve stock per line item with a short expiry (e.g. 15 minutes) tied to the order.
3. **Payment succeeds** — reservation converts to a permanent deduction (`quantity_on_hand -=`, reservation row deleted).
4. **Payment fails or the reservation window lapses** — reservation released, `quantity_on_hand` untouched. A `@Scheduled` sweep (runs every minute) deletes expired reservations that a failed/abandoned checkout left behind.
5. **Available stock** shown to shoppers = `quantity_on_hand - sum(active reservations)` for that stock item, computed at read time — no separate "available" column to keep in sync.

This is row-level accounting inside Postgres, not a distributed lock — correct and sufficient for single-instance deployment. A Redis-backed reservation layer only becomes justified if this ever needs to run as multiple app instances against high checkout concurrency, which isn't today's problem.

Low-stock is a plain `quantity_on_hand <= low_stock_threshold` check, exposed as an admin list endpoint. Once the `notification` module exists, a `LowStockEvent` published at the point of deduction is the natural hook for an email alert — not built now, but the event-driven module boundary means adding it later doesn't touch `inventory` at all.

---

## 8. Implementation roadmap

Each phase: build the new module fully (entities, migrations, service, controller, tests) → wire it in → verify it works end to end → delete the legacy package(s) it replaces. Dependency order, not arbitrary:

| Phase | Module | Deletes | Why this order |
|---|---|---|---|
| 0 | Foundation: Spring Modulith, `common` rebuild (JPA auditing, `CurrentUser`, ownership bean) | — | Everything else depends on this existing first |
| 1 | `identity` | `auth`, `user` | Nearly every other module references a user; must be stable before building on top of it. The `users` table id/shape stays compatible during this phase so legacy `order`/`address`/`payment` (not yet rewritten) keep compiling against the new `User` class via updated imports only — no simultaneous rewrite of unrelated modules forced by this phase |
| 2 | `catalog` | `product`, `category`, `tag` | Independent of everything except identity (for `created_by`/`updated_by`); needed before cart/order can reference real products |
| 3 | `inventory` | — (new) | Needs `catalog` to exist; must exist before checkout/order can reserve stock |
| 4 | `cart` | — (new) | Needs `catalog` + `inventory` |
| 5 | `order` + `pricing` | `order` | Needs `cart`, `inventory`, `identity` |
| 6 | `payment` | `payment` | Needs `order`; also removes the temporary Stage-1 `hasRole("ADMIN")` lockdown on `/payments/**`, superseded by real webhook verification |
| 7 | `shipping` | — (new) | Needs `order` |
| 8 | `review`, `wishlist` | — (new) | Independent of each other, can run in either order; need `catalog` + `identity` |
| 9 | `notification` | — (new) | Wires in via events published from already-rebuilt modules — additive, doesn't require revisiting them |
| 10 | `admin` | `admin` | Cross-cutting by definition, so it comes last — it reads from every module above |
| 11 | Frontend integration | — | Router mount, auth pages, product listing/detail, cart, checkout — visual design untouched throughout |

At the end of phase 10, the entire legacy `modules/*` tree from before this rebuild no longer exists — everything left is one of the 13 modules above. That's also the point where the Flyway history gets squashed to a single clean baseline, per §0.

Each phase is further broken into small steps when we get there (the same granularity as the Stage 1 work already done), not delivered as one large PR.

---

## 9. Open questions for approval

None blocking — the calls in §4 (cookie-based refresh + memory-held access token), §3 (order/shipment status split), and §0 (squash migrations at the end rather than continuously) are made with reasoning above rather than left open, since they're standard practice a senior engineer would just decide. Flag now if any of those should go differently before Phase 0 starts.
