# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

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

### Verified
- Full backend suite: 64/64 tests passing, `mvn compile` clean, on `master` after merging all four feature branches

#### Postman collection (`docs/api/`)
- `marketplace-api-collection.postman_collection.json` — 46 requests covering Auth, Professionals, Services, Hires, Reviews, with `pm.test` assertions and auto-chaining via `pm.environment.set`
- `marketplace-environment.postman_environment.json` — companion environment (baseUrl + tokens/ids)
- Verified end-to-end against the live stack (`docker compose up`) with Newman: 63/63 assertions pass, both on a fresh DB and on a rerun without resetting it
- Bug found and fixed during that verification: the Services folder's soft-delete test was deactivating the same service offering the Hires folder needed, 404-ing every hire creation and cascading through the rest of the run — fixed by creating a dedicated throwaway service for the delete test
