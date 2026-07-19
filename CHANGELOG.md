# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.5.0] - 2026-07-19

### Added

#### Customer-facing app (`frontend/src/prod/`)
- Public browse (`/`): professional cards (headline, category, location, star rating, hourly rate) with a category filter
- Professional detail (`/professionals/:id`): bio, active services, reviews; inline hire form per service for logged-in clients
- `/login`, `/register` (role choice CLIENT/PROFESSIONAL), nav bar that adapts to session role
- Client dashboard (`/hires`): status-badged hires, cancel for PENDING, inline review form for COMPLETED hires without one yet
- Professional dashboard (`/dashboard`): blocking create-profile form if none exists, then tabs for services (create/edit/soft-delete) and incoming hires (accept/reject/complete)
- Built by a background subagent in an isolated worktree against the live backend contract; reviewed (code read, `npm run build` + `eslint` re-run on `main`) and merged in — same pattern as the original 4-module backend build
- Root `README.md`: stack, structure, run steps, route map (`/` vs `/test`), domain model summary, testing instructions

## [0.4.0] - 2026-07-19

### Added

#### Admin seed + test console gate
- `V2__seed_admin_user.sql` — Flyway migration seeding one `ADMIN` account (`admin@marketplace.local` / `Admin123!`) since the API refuses self-registration as `ADMIN` by design and the console needed a real way in
- `frontend/src/test/` — the test console moved behind `/test`, gated by `AdminGate` (login form, only lets in sessions with `role === "ADMIN"`)
- Added `react-router-dom` for the `/` vs `/test` split

## [0.3.0] - 2026-07-15

### Added

#### Frontend (`frontend/`)
- Minimal React + Vite test console — not a product UI, one tab per feature module (Auth, Professionals, Services, Hires, Reviews) mirroring the Postman collection, each action shows the raw request/response
- JWT session kept in `localStorage` via `AuthContext`; `api.js` attaches `Authorization: Bearer` automatically
- Verified: `npm run build` clean, `eslint` clean (0 errors), and the exact request the Auth tab makes (`POST /auth/register` from origin `http://localhost:5173`) confirmed end-to-end against the live backend, including CORS preflight

### Fixed
- `docker-compose.yml`: postgres healthcheck now probes the real database (`pg_isready -d`) instead of the wrong default (a database named after the user), which was spamming `FATAL: database "marketplace_user" does not exist` into the logs every 5s

## [0.2.0] - 2026-07-15

### Added

#### Postman collection (`docs/api/`)
- `marketplace-api-collection.postman_collection.json` — 46 requests covering Auth, Professionals, Services, Hires, Reviews, with `pm.test` assertions and auto-chaining via `pm.environment.set`
- `marketplace-environment.postman_environment.json` — companion environment (baseUrl + tokens/ids)
- Verified end-to-end against the live stack (`docker compose up`) with Newman: 63/63 assertions pass, both on a fresh DB and on a rerun without resetting it
- Bug found and fixed during that verification: the Services folder's soft-delete test was deactivating the same service offering the Hires folder needed, 404-ing every hire creation and cascading through the rest of the run — fixed by creating a dedicated throwaway service for the delete test

### Verified
- Full backend suite: 64/64 tests passing, `mvn compile` clean, on `master` after merging all four feature branches

## [0.1.0] - 2026-07-15

### Added

#### Scaffold
- Spring Boot 3.2 project skeleton (Java 21, Maven), package `com.marketplace.professional`
- Domain entities: `User`, `ProfessionalProfile`, `ServiceOffering`, `Hire`, `Review`; enums `Role`, `HireStatus`
- Flyway baseline migration `V1__create_tables.sql` with full schema (users, professional_profiles, service_offerings, hires, reviews) and indices
- JWT authentication infrastructure: `JwtService`, `JwtAuthFilter`, `CustomUserDetails`, `UserDetailsServiceImpl`
- `SecurityConfig`: stateless JWT auth, public GET on `/professionals/**`, `/services/**`, `/reviews/**`; role-gated writes
- Global exception handling (`GlobalExceptionHandler`, `ResourceNotFoundException`, `BadRequestException`)
- `docker-compose.yml` (Postgres + backend), backend `Dockerfile`, `.env.example`

#### Auth (`com.marketplace.professional.auth`)
- `POST /auth/register`, `POST /auth/login` — issues JWT access tokens
- Rejects self-registration as ADMIN and duplicate emails
- 5 tests (`AuthControllerTest`)

#### Catalog (`com.marketplace.professional.catalog`)
- `ProfessionalController` (`/professionals`): create own profile, public list/search by category, get by id, update (owner or ADMIN only)
- `ServiceController` (`/services`): create offering under caller's profile, public list/search by category, get by id, update/soft-delete (owner or ADMIN only — `DELETE` sets `active=false`, no hard delete)
- 12 tests (`ProfessionalControllerTest`, `ServiceControllerTest`)

#### Hiring (`com.marketplace.professional.hiring`)
- `HireController` (`/hires`): client creates a hire against a service offering, `GET /mine` (as client or professional), get by id, `PATCH /{id}/status` with a server-enforced transition matrix (PENDING→ACCEPTED/REJECTED/CANCELLED, ACCEPTED→COMPLETED; all other transitions rejected; wrong actor for a valid transition → 403)
- 34 tests (`HireControllerTest`, `HireServiceTest` — full transition matrix)

#### Reviews (`com.marketplace.professional.review`)
- `POST /reviews/hires/{hireId}` — client-only, requires the hire to be COMPLETED, one review per hire, recalculates the professional's `avgRating`/`reviewCount`
- `GET /reviews/professionals/{professionalId}` — public
- 13 tests (`ReviewControllerTest`, `ReviewServiceTest`)
