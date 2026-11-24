# Curriculum Intelligence System
## Autonomous Agentic AI Curriculum Design & Governance Platform

[![CI/CD Pipeline](https://github.com/SulemanAG/curriculum-intelligence-system/actions/workflows/ci.yml/badge.svg)](https://github.com/SulemanAG/curriculum-intelligence-system/actions/workflows/ci.yml)
[![Java 21](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-blue.svg)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.0-blue.svg)](https://www.typescriptlang.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16%2F18-blue.svg)](https://www.postgresql.org/)
[![MCP Compliant](https://img.shields.io/badge/MCP-2024--11--05-purple.svg)](https://modelcontextprotocol.io/)

**Institutional Environment:** School of Computer Science & Engineering (AI), KLE Technological University, Hubballi  
**Project:** Academic Peer Evaluation Submission (45 Marks Project)  
**Author / Maintainer:** Suleman Agasimani  
**Repository:** [https://github.com/SulemanAG/curriculum-intelligence-system](https://github.com/SulemanAG/curriculum-intelligence-system)

---

## 1. Project Overview

The **Curriculum Intelligence System** is an enterprise-grade, agentic AI platform engineered to modernize higher-education curriculum design. Powered by the **AURA (Autonomous Universal Reasoning Agents)** multi-agent orchestration engine, the platform converts natural-language course design directives into fully formulated, accreditation-compliant institutional course plans.

The system incorporates **live external academic and industry research**, **dense vector semantic search**, **bidirectional Groq $\leftrightarrow$ Google Gemini automatic LLM failover**, and **automated document compilation** conforming to KLE Technological University's official course plan format.

---

## 2. Problem Statement

Academic curriculum design in higher education suffers from critical operational bottlenecks:
1. **Syllabus-Industry Lag:** Fast-moving domains (AI, cybersecurity, cloud architecture) evolve rapidly, leaving static 4-year curricula disconnected from contemporary employer demand.
2. **Manual Benchmarking Burden:** Academic deans and curriculum committees spend weeks manually searching peer university syllabi (IITs, NITs, MIT) and standards (ACM CS2023, IEEE).
3. **Accreditation Rigor Compliance:** Courses must strictly map to Bloom's Revised Taxonomy, Program Outcomes (PO1–PO12), and institutional credit contact hours.
4. **Lack of Evidence Backing:** Syllabi proposals often rely on individual faculty bias rather than grounded, verifiable external evidence.

---

## 3. Key Features

- **Public Landing Page & AI Workspace:** Seamless transition from educational value proposition to an interactive curriculum design workspace.
- **Five Specialist Multi-Agent Swarm (AURA):** Structured Directed Acyclic Graph (DAG) with 3 concurrent parallel research agents followed by 2 sequential synthesis agents.
- **Automatic Groq + Google Gemini Failover:** Dynamic provider failover on HTTP 429 rate limits, server outages (5xx), or timeouts, backed by an in-memory circuit breaker.
- **Deep External Research & Evidence Storage:** Google Search Grounding for live web mining, coupled with PostgreSQL full-text and Jina AI dense vector embeddings (RRF hybrid search).
- **Four Distinct Curricular Alternatives:** Dynamically synthesizes 4 pedagogical orientations (*Industry-Aligned*, *Academic Depth*, *Balanced Standard*, *Emerging Research*).
- **Master Template Preservation:** Populates the official KLE Tech master Word document (`KLE_Tech_Course_Plan_Blank_Template_No_Marks_Scheme.docx`) and compiles high-fidelity PDFs.
- **Model Context Protocol (MCP) Server:** Standard JSON-RPC 2.0 endpoints (`/api/mcp`) and stdio bridge for Claude Desktop, Cursor, and external tools.
- **OpenCLA / OpenClaw External Gateway Ready:** Governed boundary adapter for external agentic runtimes.

---

## 4. System Architecture

```
                          ┌───────────────────────────┐
                          │   React 18 + Vite Web UI  │
                          └─────────────┬─────────────┘
                                        │ HTTP / REST
                                        ▼
                          ┌───────────────────────────┐
                          │   AURA Coordinator API    │
                          │   (Spring Boot 3.4.2)     │
                          └──────┬─────────────┬──────┘
                                 │             │
                ┌────────────────┴─┐         ┌─┴────────────────┐
                │ AURA Workflow    │         │ Model Context    │
                │ Execution Engine │         │ Protocol Server  │
                └────────┬─────────┘         └──────────────────┘
                         │
        ┌────────────────┼────────────────┐
        │ PARALLEL       │                │
        ▼                ▼                ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│  Curriculum  │ │   Industry   │ │   Academic   │
│   Mapping    │ │ Skills Agent │ │ Benchmarking │
│    Agent     │ │ (Grounding)  │ │    Agent     │
└───────┬──────┘ └───────┬──────┘ └───────┬──────┘
        └────────────────┼────────────────┘
                         ▼ SEQUENTIAL
                 ┌──────────────┐
                 │ Gap Analysis │
                 │    Agent     │
                 └───────┬──────┘
                         ▼ SEQUENTIAL
                 ┌──────────────┐
                 │Recommendation│
                 │    Agent     │
                 └───────┬──────┘
                         │
                         ▼
        ┌─────────────────────────────────┐
        │  4 Course-Plan Alternatives     │
        └────────────────┬────────────────┘
                         │ Faculty Selection
                         ▼
        ┌─────────────────────────────────┐
        │ Apache POI DOCX / PDF Compiler  │
        │ (Official KLE Tech Template)    │
        └─────────────────────────────────┘
```

---

## 5. Five-Agent Workflow

| Stage | Specialist Agent | Execution Mode | Responsibilities & Tools |
| :---: | :--- | :---: | :--- |
| **1** | **Curriculum Mapping Agent** | **Parallel** | Resolves accredited course catalog, offerings, Bloom taxonomy levels, and prerequisite DAGs via `academic_catalog_retrieval`. |
| **2** | **Industry Skills Agent** | **Parallel** | Mines live industry demand, frameworks, and job competencies via Google Search Grounding & `evidence_hybrid_search`. |
| **3** | **Academic Benchmarking Agent** | **Parallel** | Surveys peer university syllabi (IIT, NIT, MIT) and ACM/IEEE CS2023 standards via Google Grounding & `evidence_keyword_search`. |
| **4** | **Gap Analysis Agent** | **Sequential** | Synthesizes institutional, industry, and benchmarking outputs; computes curriculum health score (0–100) and catalogs categorized gaps. |
| **5** | **Recommendation Agent** | **Sequential** | Dynamically formulates 4 distinct curricular proposals with citation gating and evaluation trade-off matrix. |

---

## 6. Technology Stack

- **Backend Runtime:** Java 21 LTS (OpenJDK / Eclipse Temurin)
- **Framework:** Spring Boot 3.4.2 (Spring Data JPA, Spring Security, Validation)
- **Database:** PostgreSQL 16/18 with relational schema & Flyway migrations
- **LLM Reasoning Providers:** Groq (`openai/gpt-oss-120b`) & Google Gemini (`gemini-2.5-flash`)
- **Web Search Grounding:** Google Generative AI Search Tool
- **Vector Embeddings:** Jina AI `jina-embeddings-v2-base-en` (768 dimensions)
- **Document Generation:** Apache POI 5.4.0 (DOCX) & MS Word headless / PDF Converter
- **Frontend Stack:** React 18, TypeScript 5, Vite 6, Tailwind CSS, Lucide Icons
- **Protocols & Standards:** Model Context Protocol (MCP 2024-11-05), JSON-RPC 2.0, REST, OpenAPI/Swagger

---

## 7. Folder Structure

```
curriculum-intelligence-system/
├── backend/                               # Spring Boot 3 Java 21 Backend
│   ├── src/main/java/org/curriculumai/
│   │   ├── academic/                      # Catalog, courses, departments, outcomes
│   │   ├── coordinator/                   # AURA Coordinator, DTOs, Document Service
│   │   ├── evidence/                      # Evidence storage, chunking, embeddings, hybrid search
│   │   ├── mcp/                           # Model Context Protocol JSON-RPC 2.0 Server
│   │   ├── orchestration/
│   │   │   ├── agent/                     # 5 Specialist Agents & Registry
│   │   │   ├── engine/                    # Task DAG & Workflow Execution Engine
│   │   │   ├── llm/                       # Provider Abstraction, Groq, Gemini & Circuit Breaker
│   │   │   ├── openclaw/                  # OpenCLA / OpenClaw Gateway Integration Adapter
│   │   │   ├── research/                  # External Research & Search Grounding
│   │   │   └── tools/                     # Governed Tool Registry & Audit Logging
│   ├── src/main/resources/
│   │   ├── db/migration/                  # Flyway database migration scripts (V1-V9)
│   │   └── application.yml                # Configuration file with environment mappings
│   ├── scripts/                           # MCP stdio bridge & document export scripts
│   └── pom.xml                            # Maven build specification
├── src/                                   # React + TypeScript Frontend
│   ├── components/                        # UI Components (Header, Alternatives, Feedback)
│   ├── pages/                             # LandingPage, ChatWorkspace
│   ├── services/                          # API client contracts & status polling
│   └── types/                             # TypeScript domain models & schemas
├── skills/                                # Reusable Agent Skill Specifications (Markdown)
├── .github/workflows/ci.yml               # GitHub Actions CI/CD Pipeline
├── Dockerfile.frontend                    # Multi-stage production frontend Docker image
├── docker-compose.yml                     # Full-stack Docker compose (Postgres, Backend, Frontend)
├── KLE_Tech_Course_Plan_Blank_Template_No_Marks_Scheme.docx # Official Master Template
└── README.md                              # Master Project Documentation
```

---

## 8. Prerequisites

To run this application locally, ensure you have:
1. **Java Development Kit (JDK):** Version 21 or higher (`java -version`)
2. **Apache Maven:** Version 3.8+ (`mvn -version`)
3. **Node.js & npm:** Node 20+ (`node -v`, `npm -v`)
4. **PostgreSQL:** Version 16 or 18 running locally or in Docker (`psql --version`)
5. **Git:** (`git --version`)

---

## 9. Setup Instructions

### Step 1: Clone the Repository
```bash
git clone https://github.com/SulemanAG/curriculum-intelligence-system.git
cd curriculum-intelligence-system
```

### Step 2: PostgreSQL Setup
Create the PostgreSQL database and user:
```sql
CREATE DATABASE curriculum_intelligence;
CREATE USER curriculum_app WITH ENCRYPTED PASSWORD '123';
GRANT ALL PRIVILEGES ON DATABASE curriculum_intelligence TO curriculum_app;
```

### Step 3: Environment Configuration
Copy the sample environment file in `backend/`:
```bash
cp backend/.env.example backend/.env
```
Edit `backend/.env` with your API keys:
```properties
SERVER_PORT=8081
DB_URL=jdbc:postgresql://localhost:5432/curriculum_intelligence
DB_USERNAME=curriculum_app
DB_PASSWORD=123

LLM_PRIMARY_PROVIDER=groq
LLM_FAILOVER_ENABLED=true
GROQ_API_KEY=your_groq_api_key_here
GROQ_MODEL=openai/gpt-oss-120b

GEMINI_API_KEY=your_gemini_api_key_here
GEMINI_MODEL=gemini-2.5-flash

WEB_SEARCH_PROVIDER=google_grounding
EMBEDDING_ENABLED=true
EMBEDDING_API_KEY=your_jina_key_here
```

---

## 10. Running Instructions

You need **three terminal windows**:

### Terminal 1: PostgreSQL (if not running as a Windows Service)
```bash
# Windows Command Prompt
"C:\Program Files\PostgreSQL\18\bin\postgres.exe" -D "C:\Program Files\PostgreSQL\18\data"

# Linux / macOS
pg_ctl -D /usr/local/var/postgres start
```

### Terminal 2: Spring Boot Backend
```bash
cd backend
mvn spring-boot:run
```
*Backend will start on `http://localhost:8081`. Flyway will automatically run database migrations V1 through V9.*

### Terminal 3: React Frontend
```bash
npm install
npm run dev
```
*Frontend will open on `http://localhost:5173`.*

---

## 11. Full-Stack Docker Deployment (Alternative)

To launch the complete platform in isolated Docker containers:
```bash
docker-compose up --build
```
- **Web App:** `http://localhost:5173`
- **Backend API:** `http://localhost:8081`
- **PostgreSQL:** `localhost:5432`

---

## 12. Testing Instructions

### Run Core Failover Unit Tests (12 Scenarios A through L):
```bash
mvn -f backend/pom.xml test -Dtest=LlmExecutionServiceTest
```

### Run Full Test Suite:
```bash
mvn -f backend/pom.xml test
```

### Run Frontend Type Check & Production Build:
```bash
npx tsc --noEmit
npm run build
```

---

## 13. API Endpoints Reference

| HTTP Method | Endpoint | Description |
| :---: | :--- | :--- |
| `POST` | `/api/curriculum/requests` | Ingests course design request and triggers AURA workflow. |
| `GET` | `/api/curriculum/runs/{runId}` | Polls stage progression, agent statuses, and elapsed times. |
| `GET` | `/api/curriculum/runs/{runId}/alternatives` | Retrieves 4 synthesized course plan proposals. |
| `POST` | `/api/curriculum/feedback` | Commits faculty proposal selection and rationale. |
| `GET` | `/api/curriculum/plans/{planId}` | Retrieves finalized institutional course plan. |
| `GET` | `/api/curriculum/plans/{planId}/docx` | Downloads generated Word document (KLE Tech template). |
| `GET` | `/api/curriculum/plans/{planId}/pdf` | Downloads finalized course plan PDF. |
| `GET` | `/api/curriculum/llm/status` | Runtime status of Groq/Gemini providers & circuit breaker. |
| `POST` | `/api/mcp` | Standard Model Context Protocol (MCP) JSON-RPC 2.0 endpoint. |
| `GET` | `/api/mcp/tools` | Discovers all available Governed Tools over MCP. |
| `GET` | `/api/curriculum/openclaw/status` | Honest status of OpenCLA / OpenClaw gateway adapter. |

---

## 14. Example Course Design Walkthrough

1. **Submit Request:**
   ```json
   {
     "rawPrompt": "Design an undergraduate course on Agentic AI and Autonomous Systems for 6th semester Computer Science students.",
     "department": "School of Computer Science & Engineering (AI)",
     "degreeLevel": "UG"
   }
   ```
2. **AURA Execution:** 3 parallel agents execute external search and institutional mapping.
3. **Automatic Failover:** If Groq hits TPM limits, Gemini takes over seamlessly.
4. **Alternatives:** System presents 4 proposals (*Industry*, *Academic*, *Balanced*, *Research*).
5. **Selection:** Faculty selects Alternative 1.
6. **Download:** Faculty downloads official `26ECAC401_Course_Plan.pdf` (440 KB).

---

## 15. Limitations & Future Scope

- **OpenClaw Standby:** In local setups without a dedicated OpenClaw daemon, the gateway operates in standby, transparently delegating to AURA.
- **Search Quotas:** Extensive grounding searches depend on external Google Search Grounding quotas.
- **Vector Indexing:** When dense vector embeddings are disabled, the system gracefully falls back to PostgreSQL full-text keyword search.

---

## 16. Academic Attribution & Peer Evaluation

- **Institution:** KLE Technological University, Hubballi
- **Evaluation Criteria:** 45 Marks (5 Marks $\times$ 9 Criteria)
- **Detailed Documentation:**
  - System Architecture: [ARCHITECTURE.md](ARCHITECTURE.md)
  - Agents & Skills: [AGENT_AND_SKILL_DOCUMENTATION.md](AGENT_AND_SKILL_DOCUMENTATION.md)
  - MCP & OpenClaw: [MCP_AND_OPENCLAW_INTEGRATION.md](MCP_AND_OPENCLAW_INTEGRATION.md)
  - Deep Research: [DEEP_RESEARCH_AND_EVIDENCE.md](DEEP_RESEARCH_AND_EVIDENCE.md)
  - Cloud SDLC: [CLOUD_SDLC_AND_DEPLOYMENT.md](CLOUD_SDLC_AND_DEPLOYMENT.md)
  - Demonstration Guide: [PEER_EVALUATION_DEMONSTRATION_GUIDE.md](PEER_EVALUATION_DEMONSTRATION_GUIDE.md)
  - Criteria Matrix: [PEER_EVALUATION_CRITERIA_MATRIX.md](PEER_EVALUATION_CRITERIA_MATRIX.md)
