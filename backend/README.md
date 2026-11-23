# Curriculum Intelligence System — Backend Service (Phase 2)
## Java 21 · Spring Boot 3 · PostgreSQL · Multi-Agent Coordinator

This service is the backend foundation of the **Curriculum Intelligence System (Curriculum AI)**, an academic technology application developed at KLE Technological University, Belagavi, School of Computer Science & Engineering (AI).

The backend implements the **AURA Coordinator-plus-workers** pattern described in Chapter 5 of the *Agentic AI Worklets 14-Lab Build Book*.

---

## 1. Technology Stack

- **Java Version:** 21 LTS
- **Framework:** Spring Boot 3.4.2
- **Build Tool:** Apache Maven 3.9+
- **Relational Database:** PostgreSQL 16
- **Database Migrations:** Flyway (`src/main/resources/db/migration`)
- **Persistence & ORM:** Spring Data JPA / Hibernate
- **Security:** Spring Security (Stateless, CORS enabled for React frontend)
- **API Documentation:** Springdoc OpenAPI 3 / Swagger UI (`/swagger-ui.html`)
- **Testing:** JUnit 5, Mockito, AssertJ, Spring Boot Test, H2 In-Memory (Test scope)

---

## 2. API Integration Contracts

The backend satisfies the exact integration contracts expected by the React frontend ([`src/services/api.ts`](../src/services/api.ts)):

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/health` | Backend connectivity check (`{ online: true }`) |
| `GET` | `/actuator/health` | Spring Boot Actuator health probe |
| `POST` | `/api/conversations` | Create durable conversation |
| `GET` | `/api/conversations` | List active conversations for authenticated faculty |
| `GET` | `/api/conversations/{id}` | Retrieve conversation with full message history |
| `PATCH` | `/api/conversations/{id}` | Rename conversation & lock custom title |
| `DELETE` | `/api/conversations/{id}` | Archive conversation |
| `POST` | `/api/conversations/{id}/messages` | Append message with intelligent title extraction |
| `GET` | `/api/conversations/{id}/messages` | Paginated/ordered message retrieval |
| `POST` | `/api/curriculum/requests` | Intake course design query & lock academic plan |
| `GET` | `/api/curriculum/runs/{runId}` | Poll AURA Coordinator & specialist swarm status |
| `GET` | `/api/curriculum/runs/{runId}/alternatives` | Retrieve 4 pedagogical course proposals |
| `POST` | `/api/curriculum/feedback` | Commit faculty review & rationale (Human Gate) |
| `GET` | `/api/curriculum/plans/{planId}` | Retrieve finalized institutional course plan |
| `GET` | `/api/curriculum/plans/{planId}/pdf` | Download official course plan document |

---

## 3. Local Setup & Execution

### Prerequisites
- JDK 21+ installed and configured in `JAVA_HOME`.
- Apache Maven 3.9+ installed and on PATH.
- (Optional for containerized run) Docker & Docker Compose.

### Running with Docker Compose (PostgreSQL + Backend)
```bash
cd backend
cp .env.example .env
docker compose up -d
```

### Running Locally with Maven & Dedicated PostgreSQL
1. Ensure PostgreSQL is running on port 5432 with database `curriculum_intelligence` and user `curriculum_app`.
2. Configure your environment variables (in PowerShell or Command Prompt):

   **PowerShell:**
   ```powershell
   $env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot"
   $env:Path = "$env:JAVA_HOME\bin;$env:Path"
   $env:DB_URL = "jdbc:postgresql://localhost:5432/curriculum_intelligence"
   $env:DB_USERNAME = "curriculum_app"
   $env:DB_PASSWORD = "your_actual_db_password"
   $env:SERVER_PORT = "8081"
   ```

   **Command Prompt:**
   ```cmd
   set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot
   set PATH=%JAVA_HOME%\bin;%PATH%
   set DB_URL=jdbc:postgresql://localhost:5432/curriculum_intelligence
   set DB_USERNAME=curriculum_app
   set DB_PASSWORD=your_actual_db_password
   set SERVER_PORT=8081
   ```

3. Start the Spring Boot application (port 8081):
   ```bash
   mvn spring-boot:run
   ```
4. Access Swagger UI at `http://localhost:8081/swagger-ui.html` and health probe at `http://localhost:8081/api/health`.

### Running Automated Test Suite

#### 1. Standard In-Memory Test Suite (Fast / Isolated / CI-Ready)
Executes all 35 unit, mock web, and JPA tests using in-memory H2 in PostgreSQL compatibility mode:
```bash
mvn clean test
```

#### 2. Dedicated PostgreSQL Integration Test Profile
Executes persistence integration tests against a live PostgreSQL 16/18 database on port 5432:
```bash
mvn test -Ppostgres-test
```
*Note: Ensure PostgreSQL is running and environment variable `DB_PASSWORD` or `DB_TEST_PASSWORD` is configured in your shell before running this profile.*

---

## 4. Security & Identity Architecture

### Credential Security & Rotation Guide
1. **Zero Credential Hardcoding:** Database passwords must never be committed to source control or hardcoded in configuration files. Environment variable `DB_PASSWORD` (or `POSTGRES_PASSWORD`) is resolved at process startup.
2. **Git-Ignored Files:** `.env`, `*.log`, and dynamic classpath files (`cp.txt`) are excluded in `.gitignore`. The template file `.env.example` contains placeholders only (`your_secure_db_password_here`).
3. **Safe Credential Rotation Procedure:**
   - Step 1: Update the user password in PostgreSQL:
     ```sql
     ALTER USER curriculum_app WITH PASSWORD 'new_strong_password';
     ```
   - Step 2: Update the `DB_PASSWORD` environment variable in your production secrets manager, deployment vault, or local environment.
   - Step 3: Trigger a rolling restart of the backend service. Spring Boot will establish new HikariCP pooled connections using the updated credential with zero downtime.

### Identity Resolution & Authentication Boundaries
1. **Development Identity Mode (`app.security.allow-header-identity=true`):**
   - Active strictly in development and testing profiles (`dev`, `test`).
   - Resolves identity from the caller-supplied `X-User-Id` header (defaulting to `usr-default-faculty`) to facilitate local UI and API testing.
2. **Production Principle Resolution Mode (`app.security.allow-header-identity=false`):**
   - Active in `prod` profiles.
   - Caller-supplied `X-User-Id` headers are strictly ignored and rejected. Identity is derived directly from the authenticated `SecurityContext` principal (e.g. OAuth2 JWT or institutional SSO token). Unauthenticated requests are rejected with `403 FORBIDDEN`.
3. **Fail-Fast Startup Gate:**
   - The application includes an `@PostConstruct` security validator in `UserIdentityResolver`. If `prod` profile is active and `allow-header-identity` is true, the application **immediately aborts startup** with `IllegalStateException` to prevent accidental insecure deployments.
4. **Institutional SSO Roadmap:**
   - OAuth2 / SAML / OIDC institutional single sign-on is slated for Phase 3/4. No mock SSO or fabricated token providers are claimed or installed.

### Message Role Integrity & Impersonation Prevention
- **Client Restriction:** External HTTP clients calling `POST /api/conversations/{id}/messages` are strictly restricted to role `"user"`. Submitting `role: "assistant"` or `role: "system"` results in immediate `400 BAD_REQUEST`.
- **System Orchestration:** Only trusted internal services (`AuraCoordinatorService`, specialist agents) invoke `appendAssistantMessage()` and `appendSystemMessage()`.

### Resource Ownership Enforcement
- Every conversation read, message listing, title rename, and archive operation validates ownership via `findByIdAndUserIdWithMessages(conversationId, userId)`.
- If a conversation does not belong to the requesting user, the system returns `404 NOT_FOUND` to ensure zero information disclosure regarding the existence of other faculty members' records.

