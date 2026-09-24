# LPFAP — Local Pet Foster & Adoption Portal

Course: **TPO — Distributed Programming Technologies**

A Spring Boot + JWT REST API with a vanilla-JS **multi-page** front-end that
connects animal shelters with prospective fosters and adopters. Backed by
H2 in-memory so it runs on any machine with JDK 17+ — no external DB.

## Quick start

```bash
cd lpfap
./gradlew bootRun
```

Then open <http://localhost:8080>.

### Seeded accounts

| Role  | Email                     | Password    |
|-------|---------------------------|-------------|
| ADMIN | `admin@lpfap.pl`          | `Admin1234!` |
| USER  | `jan.kowalski@example.com` | `User1234!`  |
| USER  | `anna.nowak@example.com`   | `User1234!`  |

- API docs (interactive, with **Authorize** for Bearer JWT): <http://localhost:8080/swagger-ui.html>
- Raw OpenAPI spec: <http://localhost:8080/v3/api-docs>
- H2 console: <http://localhost:8080/h2-console> (JDBC URL `jdbc:h2:mem:lpfapdb`, user `sa`, no password)

### Run on Docker

```bash
docker build -t lpfap .
docker run --rm -p 8080:8080 lpfap
```

## Configuration

| Variable | Purpose | Default |
|---|---|---|
| `LPFAP_JWT_SECRET` | HMAC key used to sign JWTs (min. 256 bits) | a development-only value, **always set your own outside local demos** |

Uploaded files (avatars, application photos) are stored in `uploads/` at runtime and are not committed.

## Tech stack

- Java 17, Spring Boot 3.4
- Spring Web, Spring Data JPA, Spring Security
- JWT (jjwt 0.12) — stateless, BCrypt cost 12
- H2 in-memory (production-ready for SQL Server / PostgreSQL via a JDBC swap)
- **Flyway** versioned migrations (`db/migration/V1__init_schema.sql`)
- **springdoc-openapi 3** at `/swagger-ui.html` + `/v3/api-docs`
- **Multi-page** vanilla-JS front-end (each section is its own real URL)
- Gradle 8 wrapper (use a JDK 17–21; Gradle 8.13 does not run on newer JDKs), Dockerfile (multi-stage on JRE 17-slim)

## Project layout

```
src/main/java/pl/edu/pjwstk/lpfap/
├── LpfapApplication.java          entry point
├── config/                        SecurityConfig, OpenApiConfig, JwtUtils, JwtAuthFilter, DataSeeder, WebConfig
├── controller/                    REST controllers + HomeController + CustomErrorController
├── service/                       business logic, @Transactional
├── repository/                    Spring Data JPA interfaces
├── dto/                           request/response DTOs (validation lives here)
├── model/                         JPA entities + enums
└── exception/                     ResourceNotFoundException, BadRequestException, GlobalExceptionHandler
src/main/resources/
├── application.yml                H2 + JPA + Flyway + JWT config
├── db/migration/V1__init_schema.sql      Flyway DDL
└── static/                        multi-page vanilla-JS front-end
    ├── index.html         /                       public animal listing
    ├── animal.html        /animal.html?id=N       single-animal page
    ├── login.html         /login.html             login + register
    ├── dashboard.html     /dashboard.html         USER · my applications
    ├── profile.html       /profile.html           USER · my profile
    ├── admin/
    │   ├── animals.html        /admin/animals.html
    │   ├── applications.html   /admin/applications.html
    │   ├── analytics.html      /admin/analytics.html
    │   └── users.html          /admin/users.html
    ├── error/{404,generic}.html  custom error pages
    ├── favicon.svg
    ├── css/app.css
    └── js/
        ├── core.js                    state, fetch helper, 401 interceptor
        ├── auth.js                    saveSession, refreshProfile, renderNav, requireAuth/Role, logout
        ├── page-animals.js            (one per page; only the script it needs loads)
        ├── page-animal-detail.js
        ├── page-login.js
        ├── page-dashboard.js
        ├── page-profile.js
        ├── page-admin-shell.js        shared admin sidebar
        ├── page-admin-animals.js
        ├── page-admin-applications.js
        ├── page-admin-analytics.js
        └── page-admin-users.js
```

The Java package layout follows the **TPO lecture/vault convention** (`model
/ dto / repository / service / controller / exception`) and the **golden
file order** (build.gradle → application config → entity → repository → DTO
→ service → controller). The front-end is a true MPA — each section is a
real HTML file at its own URL, sharing two common JS files (`core.js` for
state + fetch + the 401-interceptor, `auth.js` for nav and role guards).

## REST API (base `/api/v1`)

| Method | Path | Access | Description |
|---|---|---|---|
| POST   | `/auth/register` | Public | Register a new USER (BCrypt-hashed) |
| POST   | `/auth/login`    | Public | Returns JWT bearer token |
| POST   | `/auth/logout`   | Public | Stateless — client discards token |
| GET    | `/animals` | Public | Paged list of AVAILABLE animals. Query: `species`, `age` (young/adult/senior), `page`, `size` |
| GET    | `/animals/{id}` | Public | Single animal profile |
| GET    | `/animals/all` | ADMIN | Every animal regardless of status |
| POST   | `/animals` | ADMIN | Create animal |
| PUT    | `/animals/{id}` | ADMIN | Replace animal |
| PATCH  | `/animals/{id}/status` | ADMIN | Lifecycle status update (C-05 enforced) |
| DELETE | `/animals/{id}` | ADMIN | Soft-archive |
| GET    | `/applications/my` | USER | Own applications, newest first |
| POST   | `/applications` | USER | Submit (motivation ≥ 50 chars) |
| PATCH  | `/applications/{id}/withdraw` | USER | Withdraw own PENDING application |
| GET    | `/applications` | ADMIN | List all. Filters: `status`, `animalId`, `from`, `to` (ISO date) |
| GET    | `/applications/{id}` | ADMIN | Single application detail |
| PATCH  | `/applications/{id}/approve` | ADMIN | Approve + flip animal status + auto-reject siblings |
| PATCH  | `/applications/{id}/reject` | ADMIN | Reject |
| DELETE | `/applications/{id}` | ADMIN | Permanently delete |
| GET    | `/users/me` | USER | Own profile |
| PATCH  | `/users/me` | USER | Update own name / phone |
| GET    | `/admin/users` | ADMIN | List all users |
| PATCH  | `/admin/users/{id}/role` | ADMIN | Change a user's role |
| GET    | `/admin/reports/time-to-adoption` | ADMIN | Avg days to placement by species |
| GET    | `/admin/reports/funnel` | ADMIN | Approval/rejection rates |
| GET    | `/admin/reports/monthly?year=&month=` | ADMIN | Monthly intake/adoption counts |

All endpoints return JSON (UTF-8, ISO-8601 dates).

## Domain rules enforced

- **C-04**: BCrypt password hashing, work factor **12**; secured endpoints return **403**.
- **C-05**: An animal can never transition back to `AVAILABLE` from `ADOPTED` / `ARCHIVED`.
- **Single approval per animal**: approving one application auto-rejects every other PENDING application for the same animal, and approve fails if the animal is no longer AVAILABLE.
- **All writes** are wrapped in `@Transactional`; reads use `@Transactional(readOnly = true)`.
- **Validation** on every DTO: `@NotBlank`, `@Email`, `@Pattern` (phone regex), `@Min`, `@Max`, `@Size`. Motivation text 50–2000 chars.
- **Global error handler** maps every exception to a clean JSON `{error, status, timestamp}` body with the correct HTTP code (400 / 401 / 403 / 404 / 500).


## Build a runnable JAR

```bash
./gradlew bootJar
java -jar build/libs/lpfap-0.0.1-SNAPSHOT.jar
```
