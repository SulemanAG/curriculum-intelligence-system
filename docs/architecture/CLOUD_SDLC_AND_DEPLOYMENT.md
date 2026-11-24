# Cloud SDLC, Containerization & CI/CD Pipeline
## Reproducible Build, Automated Testing, and Multi-Cloud Deployment Guide

---

## 1. Cloud SDLC Overview

The Curriculum Intelligence System is architected for reproducible cloud builds and zero-drift deployments.

```
[Developer Git Push]
         │
         ▼
[GitHub Actions CI/CD Pipeline]
  ├── Job 1: Backend Build (Java 21, Maven compilation, Unit tests)
  ├── Job 2: Frontend Build (Node 20, TypeScript tsc, Vite production build)
  └── Job 3: Security Scan (Secret detection & .env prevention)
         │
         ▼
[Docker Containerization]
  ├── Postgres 16/18 Container (Relational schema, pgvector)
  ├── Spring Boot 3 Container (Port 8081, Temurin 21 JRE)
  └── Nginx Container (Port 80/5173, React static SPA & reverse proxy)
         │
         ▼
[Cloud Deployment Targets]
  ├── AWS ECS / Fargate (Containerized microservices)
  ├── Render / Railway / Fly.io (One-click Docker Compose)
  └── On-Premises Linux / Windows Server (Native Systemd or Docker)
```

---

## 2. GitHub Actions CI/CD Pipeline

The project includes an automated GitHub Actions workflow defined in `.github/workflows/ci.yml`.

### Automated Pipeline Jobs:
1. **`backend-test`:**
   - Checks out repository on `ubuntu-latest`.
   - Provisions Eclipse Temurin JDK 21 with Maven caching.
   - Compiles all 204 backend source files (`mvn compile -DskipTests`).
   - Runs the complete LLM failover unit test suite (`mvn test -Dtest=LlmExecutionServiceTest`).
2. **`frontend-test`:**
   - Sets up Node.js 20 with npm caching.
   - Runs `npm ci` for deterministic dependency installation.
   - Runs `npx tsc --noEmit` to verify 100% TypeScript type safety.
   - Executes `npm run build` compiling the production Vite bundle.
3. **`security-scan`:**
   - Scans git-tracked files ensuring no `.env` or credentials have been committed.

---

## 3. Docker Container Configuration

### Multi-Stage Backend Dockerfile (`backend/Dockerfile`)
```dockerfile
# Build stage (Maven + Eclipse Temurin 21)
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage (Minimal Alpine JRE)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache curl
COPY --from=builder /app/target/curriculum-intelligence-backend-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8081
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### Multi-Stage Frontend Dockerfile (`Dockerfile.frontend`)
```dockerfile
# Build stage (Node 20 Alpine)
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# Runtime stage (Nginx Alpine)
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

---

## 4. Local Full-Stack Deployment via Docker Compose

To start the entire platform with a single command:
```bash
docker-compose up --build -d
```

### Verification & Health Checking:
```bash
# Verify container statuses
docker-compose ps

# Tail backend logs
docker-compose logs -f backend

# Verify backend health
curl http://localhost:8081/actuator/health
curl http://localhost:8081/api/curriculum/llm/status
```

---

## 5. Cloud Platform Deployment Blueprint (Render / AWS)

### Deploying to Render / Railway:
1. Connect GitHub repository `https://github.com/SulemanAG/curriculum-intelligence-system`.
2. Select **Docker Compose** or create two services:
   - **Service 1 (Backend):** Environment: Docker (`backend/Dockerfile`), Port: `8081`. Add environment variables from `backend/.env.example`.
   - **Service 2 (Frontend):** Environment: Docker (`Dockerfile.frontend`), Port: `80`. Set `VITE_API_BASE_URL` to backend service URL.
   - **Database:** Managed PostgreSQL instance. Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.
