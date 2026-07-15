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
