# Nexturn Intern Management – Backend Guide

One-file reference for the Spring Boot backend: what it does, the packages it uses, how to set it up and
run it, and how it fits with the React frontend.
Last updated: 2026-10-22.

---

## 1. Overview

REST API for an internship program with four roles. Data is stored in PostgreSQL, the schema is
managed by Flyway, and login uses JWT access + refresh tokens.

| Role     | Scope                                                                                  |
| -------- | -------------------------------------------------------------------------------------- |
| `ADMIN`  | Super user. All data, plus HR management (`/api/hr`) and the admin dashboard           |
| `HR`     | All modules except HR management, limited to their own mentors and interns (`hr_id`)   |
| `MENTOR` | Their own interns' tasks, meetings, evaluations, trainings, reports                    |
| `INTERN` | Only their own data; can check in/out, apply for leave, comment on tasks               |

The frontend maps both `ADMIN` and `HR` to the UI role "HR". See `UI_FEATURE_ANALYSIS.md` in the
frontend project.

---

## 2. Tech Stack and Packages (`pom.xml`)

| Package / tool                              | Used for                                                     |
| ------------------------------------------- | ------------------------------------------------------------ |
| Java 17, Maven wrapper (`mvnw`)             | Language and build (no global Maven needed)                  |
| Spring Boot 4.0.8 `starter-webmvc`          | REST controllers                                             |
| `starter-data-jpa` (Hibernate)              | ORM and repositories                                         |
| `starter-security` + `jjwt` (api/impl/jackson) | Stateless JWT authentication and role checks              |
| `starter-validation`                        | Request DTO validation                                       |
| `starter-flyway` + `flyway-database-postgresql` | Versioned DB migrations (`db/migration`, V1–V19)         |
| `postgresql` driver                         | Production database                                          |
| `h2` (+ `spring-boot-h2console`)            | In-memory DB for local experiments                           |
| `springdoc-openapi-starter-webmvc-ui`       | Swagger UI at `/swagger-ui.html`                             |
| `poi-ooxml` (Apache POI)                    | Excel (`.xlsx`) report export                                |
| `lombok`                                    | Boilerplate reduction                                        |
| `spring-boot-devtools`                      | Auto-restart in development                                  |
| JUnit 5, AssertJ, Spring test starters      | Unit and integration tests                                   |

---

## 3. Project Structure (`src/main/java/com/nexturn/internmanagement`)

Feature-first packages. Each domain has `Entity`, `Repository`, `Service`, `Controller` and a `dto/` folder.

```
announcement/ attendance/ auth/ certificate/ dashboard/ document/ evaluation/ feedback/
hr/ intern/ leave/ meeting/ mentor/ notification/ ppo/ report/ settings/ task/ training/ user/
common/    ApiResponse, ErrorResponse, GlobalExceptionHandler, ResourceNotFound/LoginExpired exceptions
config/    SecurityConfig, CorsConfig, OpenApiConfig, JpaAuditingConfig, SchedulingConfig
security/  JwtService, JwtAuthFilter, UserDetailsServiceImpl, UserPrincipal, OwnershipService
resources/ application.yml, application.properties, db/migration/V1..V19
uploads/   Stored document files (configurable, see section 5)
```

Every endpoint returns the envelope `{ "success": true, "data": ..., "message": "..." }`
(`ApiResponse`). Errors go through `GlobalExceptionHandler`. The frontend `services/api.ts` unwraps it.

---

## 4. Key Features

- **Authentication:** login, refresh, logout, current user, change password. Access token expires in
  15 minutes and the refresh token in 7 days. Malformed or expired tokens return a clean error, never a 500.
- **Role-based access and data scoping:** `OwnershipService` decides what each user may see
  (`internScope`, `mentorScope`, `hrOwnsMentor`, `hrOwnsIntern`, ...). Mentors see only their interns,
  interns only themselves, HR only their assigned people.
- **HR module (ADMIN only):** create HR users, assign mentors and interns with
  `PUT /api/hr/{id}/mentors|interns` and body `{"ids":[...]}`. A blank password defaults to `Hr@nexturn123`.
- **Auto-provisioned logins:** creating a mentor or intern also creates their user account.
- **Attendance:** daily check-in and check-out with worked seconds. A background job
  (`autoCheckOutExpiredSessions`, runs every 60 s) automatically checks out sessions that run too long.
- **Leave requests:** interns apply; HR approves or rejects (approval is reflected in attendance).
- **Tasks:** status and progress, work updates (`PATCH /{id}/work`), threaded comments with edit flag,
  "Not Started" status.
- **Evaluations:** criteria-based ratings, optionally linked to a completed task.
- **Documents:** multipart upload (max 10 MB) and download. Files are stored on disk.
- **Notifications:** per-recipient, with `mark-read` and a link to the related item.
- **Reports:** per-intern aggregate and a real `.xlsx` export (Apache POI).
- **Dashboards:** admin, mentor and intern summaries (`/api/dashboard/...`).
- **Settings:** per-user notification, theme and phone preferences.
- **Other modules:** training, certificates, announcements (target audience), PPO (pre-placement
  offer), feedback, users.

---

## 5. Configuration

Two config files exist. `application.properties` takes precedence over `application.yml` for the same key.

| Setting                  | `application.yml`                              | `application.properties` (wins)     |
| ------------------------ | ---------------------------------------------- | ----------------------------------- |
| `server.port`            | `8080`                                         | **`8082`** (what the frontend uses) |
| `spring.jpa...ddl-auto`  | `validate` (Flyway owns the schema)            | `update`                            |
| DB URL / user / password | `localhost:5432/intern_management`, `postgres` | same database                       |

Recommendation: keep one file as the source of truth. For Flyway-managed schemas `ddl-auto: validate`
is the safe value, so remove the conflicting keys from `application.properties`.

Other settings in `application.yml`:

```yaml
spring.servlet.multipart.max-file-size: 10MB
app.cors.allowed-origins: http://localhost:5173,http://localhost:5174
app.file-storage.location: uploads        # optional, default "uploads"
jwt.secret: ${JWT_SECRET:local-dev-secret-change-me-in-prod-0123456789}
jwt.access-token-expiry-ms: 900000        # 15 min
jwt.refresh-token-expiry-ms: 604800000    # 7 days
springdoc.swagger-ui.path: /swagger-ui.html
```

- Set `JWT_SECRET` (32+ random chars) as an environment variable outside local development.
- Open endpoints: `/api/auth/login`, `/api/auth/refresh`, Swagger, `/h2-console/**`, `/actuator/health`.
  Everything else needs `Authorization: Bearer <accessToken>`.
- Frontend must point to the backend with `VITE_API_BASE_URL=http://localhost:8082/api`.

---

## 6. Prerequisites

1. **JDK 17+** (Temurin 17 or 21): https://adoptium.net/, then check with `java -version`.
2. **PostgreSQL 16+**: set a password for the `postgres` user and keep port `5432`.
3. **Git** and an IDE (VS Code with _Extension Pack for Java_ and _Spring Boot Extension Pack_, or IntelliJ).
4. Maven is **not** required, because the project ships `mvnw` / `mvnw.cmd`.

---

## 7. Setup and Run

```powershell
# 1. Create databases (use the postgres password from step 6)
& "C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres
```

```sql
CREATE DATABASE intern_management;
CREATE DATABASE intern_management_test;   -- used only by the automated tests
\q
```

```powershell
# 2. Match the DB password in application.yml / application.properties (default: postgres)
# 3. Build
.\mvnw.cmd clean compile

# 4. Run (Flyway applies V1..V19 on first start)
.\mvnw.cmd spring-boot:run
```

Expected log: `Started InternManagementBackendApplication in X seconds`.
If the port is busy, start with a different one: `$env:SERVER_PORT = "8083"` before running.

### Verify

- Swagger UI: `http://localhost:8082/swagger-ui.html`
- Login as the seeded admin:
  ```powershell
  curl -X POST http://localhost:8082/api/auth/login `
    -H "Content-Type: application/json" `
    -d '{"email":"admin@nexturn.com","password":"Admin@123"}'
  ```
  Expected `200` with `data.accessToken`.
- Call a protected endpoint:
  ```powershell
  curl http://localhost:8082/api/interns -H "Authorization: Bearer <accessToken>"
  ```

Seeded and default logins (development only, change them for any shared environment):

| Purpose              | Username / source          | Password                                                         |
| -------------------- | -------------------------- | ---------------------------------------------------------------- |
| Postgres superuser   | `postgres`                 | Set during install                                               |
| App admin            | `admin@nexturn.com`        | `Admin@123`                                                      |
| New HR (blank input) | created through `/api/hr`  | `Hr@nexturn123`                                                  |
| Mentor / intern      | auto-created accounts      | Backend default (see migrations V7 and V8 and the services)      |
| JWT secret           | env var `JWT_SECRET`       | Falls back to the local dev value above                          |

---

## 8. API Reference (all under `/api`)

| Area           | Endpoints                                                                                       |
| -------------- | ----------------------------------------------------------------------------------------------- |
| Auth           | `POST /auth/login`, `/auth/refresh`, `/auth/logout`, `/auth/change-password`; `GET /auth/user`  |
| Users          | `/users` CRUD, `PUT /users/{id}/role`                                                           |
| HR (ADMIN)     | `/hr` CRUD, `GET/PUT /hr/{id}/mentors`, `GET/PUT /hr/{id}/interns`                              |
| Mentors        | `/mentors` CRUD, `GET /mentors/{id}/interns`                                                    |
| Interns        | `/interns` CRUD, `GET /interns/{id}/dashboard`                                                  |
| Tasks          | `/tasks` CRUD, `GET /tasks/stats`, `PATCH /tasks/{id}/work`                                     |
| Task comments  | `/tasks/{taskId}/comments` (list, add), `PUT .../{commentId}`                                   |
| Attendance     | `/attendance` CRUD, `GET /today`, `POST /check-in`, `POST /check-out`, `GET /calendar`, `/stats` |
| Leaves         | `GET/POST /leaves`, `PUT /leaves/{id}/approve`, `PUT /leaves/{id}/reject`                       |
| Meetings       | `/meetings` CRUD, `GET /meetings/upcoming`                                                      |
| Training       | `/training` CRUD, `/training/stats`                                                             |
| Evaluations    | `/evaluations` CRUD, `/evaluations/stats`                                                       |
| PPO            | `/ppo` CRUD, `GET /ppo/my`, `/ppo/stats`                                                        |
| Certificates   | `/certificates` CRUD, `/certificates/stats`                                                     |
| Documents      | `GET /documents`, `GET /latest`, `POST /upload`, `GET /{id}/download`, `DELETE /{id}`           |
| Announcements  | `/announcements` CRUD, `/announcements/stats`                                                   |
| Notifications  | `/notifications` CRUD, `PUT /notifications/{id}/mark-read`                                      |
| Feedback       | `GET /feedback`, `GET /feedback/my`, `POST`, `DELETE /{id}`                                     |
| Reports        | `GET /reports`, `GET /reports/export` (.xlsx), `GET /reports/intern/{internId}`                 |
| Settings       | `GET/PUT /settings/user`                                                                        |
| Dashboard      | `GET /dashboard/admin`, `/dashboard/mentor/{mentorId}`, `/dashboard/intern/{internId}`          |

Note: the authenticated-user endpoints were renamed from `/auth/me` to `/auth/user` and
`/settings/me` to `/settings/user`.

---

## 9. Database

- Schema is created only by Flyway migrations in `src/main/resources/db/migration`:
  - V1–V3: users, mentors, interns, tasks, attendance, meetings
  - V4, V6: seeded admin user (password hash fix)
  - V5: training, evaluation, document, certificate, announcement, notification, PPO, feedback, settings
  - V7, V8: back-fill login accounts for mentors and interns
  - V9: `HR` role and `hr_id` ownership on mentors and interns
  - V10–V11: attendance check-in/out and worked seconds
  - V12: leave requests
  - V13–V14, V18: task "not started" status, comments, comment edited flag
  - V15: intern end date
  - V16–V17: notification link and back-fill
  - V19: evaluation task and criteria
- Never edit an applied migration. Add a new `V20__...sql` instead.

---

## 10. Tests

```powershell
.\mvnw.cmd test        # needs the intern_management_test database
```

Test classes (about 37 `@Test` methods):

- `AuthFlowTest`: login success and failure, token validation, `/api/auth/user`
- `OwnershipAuthorizationTest`: mentor, intern and HR data scoping and cross-role 403s
- `ReportExportServiceTest`: Excel export
- `InternManagementBackendApplicationTests`: context loads
- `BaseIntegrationTest`: shared test setup

Re-run the suite after any change to endpoints, security or migrations to confirm the current result.

---

## 11. Frontend Integration

- Frontend repo path: `Nexturn-Intern-Management---Frontend` (React 19, RTK Query, MUI).
- It calls `http://localhost:8082/api`, adds the bearer token, and refreshes it on 401/403.
- CORS allows `http://localhost:5173` and `5174`. Add other origins in `app.cors.allowed-origins`.
- Frontend demo script: `DEMO_GUIDE.md`. Frontend reference: `UI_FEATURE_ANALYSIS.md`.

---

## 12. Troubleshooting

| Problem                                | Fix                                                                                       |
| -------------------------------------- | ----------------------------------------------------------------------------------------- |
| `psql` not found                       | Use the full path or add `...\PostgreSQL\16\bin` to PATH                                  |
| `password authentication failed`       | Make the DB password in the config file match your Postgres password                      |
| Frontend cannot reach the API          | Check `VITE_API_BASE_URL` matches the running port (8082 by default) and CORS origins     |
| Port already in use                    | Stop the other process or set `SERVER_PORT`                                               |
| Flyway validation error                | A migration was edited after being applied; restore it and add a new migration            |
| Schema differs from entities           | Check that `ddl-auto` is not `update` in `application.properties` (see section 5)         |
| Upload fails                           | File must be 10 MB or smaller; check write access to the `uploads/` folder                |
| Tests fail with DB connection errors   | Create the `intern_management_test` database                                              |
| `mvnw` permission denied               | On Windows use `.\mvnw.cmd`                                                               |

---

## 13. Known Gaps and Ideas

- Conflicting `application.yml` and `application.properties` (port and `ddl-auto`); keep one.
- Database credentials and default passwords are in config and migrations; move them to environment
  variables or a secrets store before deployment.
- No pagination on list endpoints; add `Pageable` support if the data grows.
- `/h2-console` is open in the security config; restrict or remove it outside local development.
- Possible next steps: refresh-token rotation cleanup job, audit log, Docker Compose for Postgres + API.
