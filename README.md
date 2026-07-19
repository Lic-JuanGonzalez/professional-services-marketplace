# Professional Services Marketplace

Platform where clients hire professionals for services and leave reviews. Spring Boot + PostgreSQL backend, JWT auth, React frontend.

## Stack

- **Backend:** Java 21, Spring Boot 3.2 (Web, Security, Data JPA, Validation), PostgreSQL 16, Flyway, JWT (jjwt), MapStruct, SpringDoc/OpenAPI
- **Frontend:** React + Vite (plain JS), React Router
- **Infra:** Docker Compose (Postgres + backend)
- **Tests:** JUnit 5 + Mockito (backend), Postman/Newman collection (API), manual UI verification (frontend)

## Project structure

```
backend/    Spring Boot API (port 8082, context path /api)
frontend/   React app (port 5173) — public site + admin-gated test console
docs/api/   Postman collection + environment
```

## Run everything

```bash
cp .env.example .env   # optional, defaults work out of the box
docker compose up -d --build   # postgres + backend
cd frontend && npm install && npm run dev
```

- Backend: `http://localhost:8082/api` (health: `/api/actuator/health`, docs: `/api/swagger-ui.html`)
- Frontend: `http://localhost:5173`

## Frontend routes

- **`/`** — customer-facing app: browse professionals, register/login, hire, review (client side); profile/services/incoming-hires dashboard (professional side)
- **`/test`** — raw request/response test console (mirrors the Postman collection), gated behind an admin login

Admin account is seeded automatically by a Flyway migration (`backend/src/main/resources/db/migration/V2__seed_admin_user.sql`):

```
email:    admin@marketplace.local
password: Admin123!
```

## Domain model

- **User** — `CLIENT`, `PROFESSIONAL`, or `ADMIN` (self-registration as `ADMIN` is blocked)
- **ProfessionalProfile** — one per professional user; headline, bio, category, location, hourly rate, `avgRating`/`reviewCount` (auto-computed)
- **ServiceOffering** — published under a professional's profile; soft-deletable (`active` flag, never hard-deleted)
- **Hire** — a client's request to hire a service offering, with a server-enforced status transition matrix:
  - `PENDING` → `ACCEPTED` / `REJECTED` (professional) or `CANCELLED` (client)
  - `ACCEPTED` → `COMPLETED` (professional)
  - all other transitions rejected; wrong actor for a valid one → 403
- **Review** — one per hire, only after it's `COMPLETED`; recalculates the professional's rating

## Testing

- Backend: `cd backend && mvn test` (64 tests)
- API: import `docs/api/marketplace-api-collection.postman_collection.json` + environment into Postman, or run with Newman:
  ```bash
  npx newman run docs/api/marketplace-api-collection.postman_collection.json \
    -e docs/api/marketplace-environment.postman_environment.json
  ```

See `CHANGELOG.md` for the full history of what was built and why.
