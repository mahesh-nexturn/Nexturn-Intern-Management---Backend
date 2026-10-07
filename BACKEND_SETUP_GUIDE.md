# Intern Management Backend — Step-by-Step Setup Guide

This guide walks you through installing every tool, configuring passwords/keys, opening the
project in VS Code, and fully verifying the Spring Boot backend (`intern-management-backend/`)
built from `BACKEND_ARCHITECTURE.md`.

---

## 1. Prerequisites — Install Everything

### 1.1 Java 21 (JDK)
1. Download **Eclipse Temurin JDK 21** (or any JDK 17+): https://adoptium.net/
2. Run the installer, check **"Set JAVA_HOME variable"** and **"Add to PATH"**.
3. Verify:
   ```powershell
   java -version
   ```
   Expected: `openjdk version "21..."`

### 1.2 Maven (optional — the project ships its own wrapper)
The repo includes `mvnw` / `mvnw.cmd`, so a separate Maven install is **not required**.
If you want a global Maven anyway: https://maven.apache.org/download.cgi, then add `bin/` to PATH.

### 1.3 PostgreSQL 16
1. Download installer: https://www.postgresql.org/download/windows/
2. Run it and **set a password for the `postgres` superuser** when prompted
   — this is the **"password step"**. Remember it; you'll enter it again in step 3.
3. Keep the default port `5432`.
4. Verify install:
   ```powershell
   & "C:\Program Files\PostgreSQL\17\bin\psql.exe" --version
   ```

### 1.4 VS Code + Extensions
1. Download VS Code: https://code.visualstudio.com/
2. Install these extensions (Extensions view → search each):
   - **Extension Pack for Java** (Microsoft)
   - **Spring Boot Extension Pack** (VMware/Broadcom)
   - **Community Server Connectors** (optional, for DB browsing)
3. Reload VS Code after installing.

### 1.5 Git (if not already installed)
https://git-scm.com/download/win

---

## 2. Create the Databases

Open a terminal and connect with `psql` using the password you set in step 1.3:

```powershell
& "C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres
```
It will prompt: `Password for user postgres:` → enter the password from step 1.3.

Then run:
```sql
CREATE DATABASE intern_management;
CREATE DATABASE intern_management_test;   -- used only by the automated test suite
\q
```

---

## 3. Configure Passwords & Secret Keys

Open `intern-management-backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/intern_management
    username: postgres
    password: postgres        # <-- change to the password you set in step 1.3

jwt:
  secret: ${JWT_SECRET:local-dev-secret-change-me-in-prod-0123456789}
```

- **Database password**: replace `password: postgres` with your actual Postgres password
  (or leave `postgres`/`postgres` if that's what you used).
- **JWT secret key**: this signs login tokens. For local dev the default value works.
  For anything beyond local dev, set an environment variable instead of editing the file:
  ```powershell
  $env:JWT_SECRET = "a-long-random-string-at-least-32-chars"
  ```

The test profile (`src/test/resources/application-test.yml`) points at `intern_management_test`
with its own dedicated JWT secret — no changes needed there for local runs.

---

## 4. Open the Project in VS Code

```powershell
cd "C:\Users\MaheshBetha\Downloads\Nexturn-Intern-Management---Frontend-main\Nexturn-Intern-Management---Frontend-main"
code intern-management-backend
```
VS Code will detect the Maven project and start downloading Java language server support —
wait for the bottom-right progress spinner to finish.

---

## 5. Build the Application

```powershell
cd intern-management-backend
.\mvnw.cmd clean compile
```
Expected: `BUILD SUCCESS`. First run downloads all dependencies (~2–5 minutes).

---

## 6. Run the Application

Option A — VS Code: open `InternManagementBackendApplication.java` → click **Run** (▶) above `main`.

Option B — terminal:
```powershell
.\mvnw.cmd spring-boot:run
```

On startup you should see Flyway apply 5 migrations and:
```
Started InternManagementBackendApplication in X seconds
```
By default, the API is live at `http://localhost:8080`.

If port `8080` is already in use, start the backend on port `8081` instead:
```powershell
$env:SERVER_PORT = "8081"
.\mvnw.cmd spring-boot:run
```
In that case, use `http://localhost:8081` for the verification URLs below.

---

## 7. Verify It Works

The URLs below use port `8080`. If you used the port-conflict workaround in step 6,
replace `8080` with `8081` in each URL below.

### 7.1 Swagger UI
Open in a browser: `http://localhost:8080/swagger-ui.html`

### 7.2 Login with the seeded admin account
```powershell
curl -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{"email":"admin@nexturn.com","password":"Admin@123"}'
```
Expected: `200 OK` with `data.accessToken` in the response.

| Field    | Value              |
|----------|--------------------|
| Email    | admin@nexturn.com  |
| Password | Admin@123          |
| Role     | ADMIN              |

### 7.3 Call a protected endpoint
```powershell
$token = "<paste accessToken here>"
curl http://localhost:8080/api/interns -H "Authorization: Bearer $token"
```
Expected: `200 OK` with a JSON list (empty array initially).

---

## 8. Run the Automated Test Suite

Requires the `intern_management_test` database from step 2.

```powershell
.\mvnw.cmd test
```
Expected: `Tests run: 17, Failures: 0, Errors: 0` and `BUILD SUCCESS`. Covers:
- `AuthFlowTest` — login success/failure, token validation (5 tests)
- `OwnershipAuthorizationTest` — mentor/intern data scoping, cross-role 403s (9 tests)
- `ReportExportServiceTest` — Excel export generation (2 tests)
- `InternManagementBackendApplicationTests` — Spring context loads (1 test)

---

## 9. Feature Checklist (all verified)

- [x] Auth: login / refresh / logout / me / change-password
- [x] Users, Mentors, Interns, Tasks, Attendance, Meetings CRUD
- [x] Training, Evaluation, Document, Certificate, Announcement, Notification, PPO, Feedback, Settings modules
- [x] Ownership-scoped authorization (mentors see only their interns; interns see only themselves)
- [x] `/api/reports` and `/api/reports/export` (real `.xlsx` generation via Apache POI)
- [x] JWT auth with access + refresh tokens; malformed/invalid tokens rejected safely (no 500s)
- [x] Automated JUnit integration + unit tests against a real Postgres test database

---

## 10. Troubleshooting

| Problem | Fix |
|---|---|
| `psql` not found | Use full path: `C:\Program Files\PostgreSQL\16\bin\psql.exe`, or add that folder to PATH |
| `FATAL: password authentication failed` | Re-check `application.yml` password matches what you set during Postgres install |
| Port `8080` already in use | Leave the existing process running and start the backend on port `8081` using the command in step 6; use port `8081` for the verification URLs |
| `mvnw` permission denied | Run `.\mvnw.cmd` (Windows uses the `.cmd` wrapper, not `./mvnw`) |
| Tests fail with DB connection errors | Confirm `intern_management_test` database exists (step 2) |

---

## 11. Default Credentials Summary

| Purpose | Username | Password |
|---|---|---|
| Postgres superuser | `postgres` | *(set during install, step 1.3)* |
| App admin login | `admin@nexturn.com` | `Admin@123` |
| JWT signing secret | *(env var)* `JWT_SECRET` | defaults to `local-dev-secret-change-me-in-prod-0123456789` for local dev only |
