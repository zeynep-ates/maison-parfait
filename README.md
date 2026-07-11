# Maison Parfait

E-commerce platform for a premium French patisserie. The frontend is a finished, deliberately-designed React storefront; the backend is being rebuilt from scratch as a modular monolith, module by module, replacing an earlier prototype.

## Stack

- **Backend**: Spring Boot 4.1.0, Java 21, PostgreSQL, Flyway, Spring Security, JWT, Spring Modulith, MapStruct, Testcontainers
- **Frontend**: React 19, Vite, Tailwind CSS
- **Infra**: Docker Compose (Postgres, pgAdmin, backend)

## Architecture

The backend is a modular monolith rebuilt one module at a time, with each new module fully replacing its legacy counterpart before the old code is deleted. Full design decisions and rationale live in:

- [`docs/backend-architecture.md`](docs/backend-architecture.md) - overall module map, database design, payment/shipping abstractions, and the phased rebuild roadmap
- [`docs/identity-module-design.md`](docs/identity-module-design.md) - authentication, sessions, and token design for the identity module

### Status

The **identity** module (registration, email verification, login, JWT access tokens, refresh token rotation with reuse detection, per-device session management) is implemented. Password reset, email change, and every other domain module (catalog, cart, order, payment, shipping, etc.) are not yet rebuilt - see the roadmap in `docs/backend-architecture.md` for what's next.

## Running locally

### Backend + database

```bash
cd backend
docker compose up -d postgres      # Postgres on :5432
./mvnw spring-boot:run             # dev profile is the default; API on :8080
```

Or run the backend itself in a container too:

```bash
cd backend
docker compose up -d
```

API docs (Swagger UI) once running: `http://localhost:8080/swagger-ui.html`

### Frontend

```bash
cd frontend
npm install
npm run dev                        # http://localhost:5173
```

### Tests

```bash
cd backend
./mvnw test                        # unit tests
./mvnw verify                      # includes Testcontainers-backed integration tests; requires Docker
```
