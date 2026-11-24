# System Architecture & Technical Specifications
## Curriculum Intelligence System — AURA Multi-Agent Engine

---

## 1. Architectural Overview

The **Curriculum Intelligence System** is built on a clean multi-tiered architecture that decouples user interactions, workflow orchestration, dynamic reasoning, external research, and document synthesis.

```
+-----------------------------------------------------------------------------------+
|                                  PRESENTATION LAYER                                |
|  - React 18 + Vite Web Application (Tailwind CSS, Lucide Icons)                  |
|  - Landing Page & Conversational AI Curriculum Workspace                          |
|  - Real-time Stage Polling, Alternative Comparison, Feedback Form                 |
+---------------------------------------------------------+-------------------------+
                                                          | REST / JSON
                                                          v
+-----------------------------------------------------------------------------------+
|                                COORDINATION LAYER                                 |
|  - AuraCoordinatorService: Workflow lifecycle, Run dispatching, Feedback logging  |
|  - CurriculumController: REST Ingestion & Proposal Endpoints                      |
|  - McpServerController: Standard JSON-RPC 2.0 Model Context Protocol Endpoint     |
|  - OpenClawGatewayService: External agentic runtime adapter                       |
+---------------------------------------------------------+-------------------------+
                                                          |
                                                          v
+-----------------------------------------------------------------------------------+
|                             AURA ORCHESTRATION ENGINE                             |
|  - TaskExecutionEngine: Deterministic DAG Dependency Evaluation                   |
|  - WorkflowEventRepository: Transactional Event Log & Audit Trail                 |
|  - GovernedToolRegistry: Strict Agent-to-Tool Security Enforcement                |
+-------------------+--------------------+--------------------+---------------------+
                    |                    |                    |
                    v                    v                    v
+------------------------+ +------------------------+ +------------------------+
|   Curriculum Mapping   | |  Industry Skills Agent | | Academic Benchmarking  |
|   (Institutional DB)   | |  (Search Grounding)    | | (ACM/IEEE & Peer Syllabi
+------------------------+ +------------------------+ +------------------------+
                    \                    |                    /
                     +-------------------+-------------------+
                                         |
                                         v
                         +-------------------------------+
                         |       Gap Analysis Agent      |
                         |   (Multi-Perspective Deficits)|
                         +---------------+---------------+
                                         |
                                         v
                         +-------------------------------+
                         |     Recommendation Agent      |
                         | (4 Proposals & Trade-offs)    |
                         +---------------+---------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
|                             LLM REASONING & FAILOVER                              |
|  - LlmExecutionService: Automatic Failover Coordinator                            |
|  - ProviderHealthTracker: Circuit Breaker (CLOSED / OPEN / HALF_OPEN)             |
|  - GroqLlmProvider (openai/gpt-oss-120b) <---> GeminiLlmProvider (gemini-2.5-flash)
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
|                             DATA & PERSISTENCE LAYER                              |
|  - PostgreSQL 16/18: Relational entities, Flyway migrations V1 through V9         |
|  - Evidence Repository: Discovered sources, full-text chunks, vector embeddings   |
|  - Document Generator: Apache POI official KLE Tech DOCX & PDF conversion         |
+-----------------------------------------------------------------------------------+
```

---

## 2. AURA Directed Acyclic Graph (DAG) Execution Flow

AURA manages the execution of tasks via an immutable DAG state machine. 

```
                                [Course Request Intake]
                                           │
                                           ▼
                                [Parallel View-Building]
                        ┌──────────────────┼──────────────────┐
                        │                  │                  │
                        ▼                  ▼                  ▼
                [Curriculum Mapping]  [Industry Skills]  [Academic Benchmarks]
                        │                  │                  │
                        └──────────────────┼──────────────────┘
                                           │ (All 3 Must Succeed)
                                           ▼
                                  [Curriculum Gap Analysis]
                                           │ (Requires Upstream Findings)
                                           ▼
                                [Proposal Recommendation]
                                           │ (Requires Gap Analysis)
                                           ▼
                                  [Faculty Review Gate]
                                           │
                                           ▼
                             [Final Institutional Document]
```

### Concurrency Guarantees
1. **Parallel Phase:** `CurriculumMappingAgent`, `IndustrySkillsAgent`, and `AcademicBenchmarkingAgent` execute simultaneously across managed thread workers.
2. **Sequential Barrier:** `GapAnalysisAgent` verifies that all three upstream task records exist and contain non-empty findings. If any task is missing or blocked, Gap Analysis halts honestly.
3. **Synthesis Barrier:** `RecommendationAgent` consumes the combined outputs of all 4 prior stages.

---

## 3. Database Schema & Flyway Migrations

The database schema is strictly governed and versioned using Flyway migrations:

- **`V1__initial_schema.sql`:** Core academic entities (`departments`, `programmes`, `courses`, `curriculum_versions`).
- **`V2__outcomes_and_mappings.sql`:** Program Outcomes (PO1–PO12), Course Outcomes (CO1–CO5), and CO–PO correlation matrix.
- **`V3__evidence_repository.sql`:** `discovered_sources`, `evidence_documents`, `evidence_chunks`.
- **`V4__vector_embeddings.sql`:** Jina AI dense vector embeddings and pgvector similarity index.
- **`V5__aura_orchestration.sql`:** `analysis_runs`, `workflow_tasks`, `workflow_events`, `agent_definitions`.
- **`V6__proposals_and_alternatives.sql`:** `course_plan_alternatives`, `evaluation_matrices`.
- **`V7__feedback_and_audit.sql`:** `feedback_records`, `preference_datasets`.
- **`V8__final_course_plans.sql`:** Finalized institutional course plans and document metadata.
- **`V9__governed_tool_registry.sql`:** Tool permissions, execution counters, and security audit logs.

---

## 4. LLM Provider Failover Engine & Circuit Breaker

```
               [Incoming Agent Reasoning Request]
                               │
                               ▼
                    Is Primary Circuit Closed?
                     /                      \
               YES  /                        \  NO (OPEN / Cool-down)
                   ▼                          ▼
          [Execute on Groq]           [Direct Route to Gemini]
            /           \
     SUCCESS             FAIL (429/5xx/Timeout)
        │                       │
        ▼                       ▼
    [Return Result]      [Record Failure & Trip Circuit]
                                │
                                ▼
                        [Execute on Gemini]
                          /           \
                   SUCCESS             FAIL
                      │                   │
                      ▼                   ▼
              [Return Result]     [Throw Honest LlmException]
```

- **Zero-Latency Circuit Bypass:** When a provider trips to `OPEN`, subsequent requests bypass the failed provider immediately without wasting connection timeouts.
- **Cooldown Interval:** Enforces a 30-second back-off before transitioning to `HALF_OPEN` for a single trial request.
- **Safe Telemetry:** Logs provider switches and stores failover provenance without printing API keys or authorization headers.

---

## 5. KLE Tech Document Generation Pipeline

```
[Selected Alternative DTO] ──► [KleTechDocumentService]
                                      │
                                      ├─► Loads Master Template (.docx)
                                      ├─► Injects Course Metadata (Code, Title, Credits)
                                      ├─► Populates 5-Column Course Units Table
                                      ├─► Populates 4-Column Laboratory Experiments Table
                                      ├─► Injects Reference Books & Citations
                                      │
                                      ▼
                       [Generated Clean .docx] (86 KB)
                                      │
                                      ▼ (Headless Word / PDF Engine)
                       [Generated Clean .pdf]  (440 KB)
```

The service preserves university logos, department headers, font styles, and table borders, guaranteeing that downloaded plans are ready for Board of Studies submission.
