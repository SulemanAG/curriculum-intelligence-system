# Curriculum Intelligence System — Documentation Index

Welcome to the comprehensive technical documentation for the **Curriculum Intelligence System**. This directory contains architectural specifications, peer evaluation rubrics, verification audits, and integration guides for the KLE Technological University academic peer review.

---

## 📂 Directory Structure

### 1. 🏛️ Architecture & Technical Specifications (`docs/architecture/`)
Deep technical designs and runtime contracts for all system subsystems:
- **[ARCHITECTURE.md](file:///c:/Users/Suleman%20Agasimani/OneDrive/Desktop/Agentic/new/docs/architecture/ARCHITECTURE.md)** — Core platform architecture, AURA DAG orchestration engine, concurrent multi-agent design, and data flow pipelines.
- **[AGENT_AND_SKILL_DOCUMENTATION.md](file:///c:/Users/Suleman%20Agasimani/OneDrive/Desktop/Agentic/new/docs/architecture/AGENT_AND_SKILL_DOCUMENTATION.md)** — Complete specification of all 5 specialist agents and 11 governed skills (schemas, prompts, failure modes).
- **[MCP_AND_OPENCLAW_INTEGRATION.md](file:///c:/Users/Suleman%20Agasimani/OneDrive/Desktop/Agentic/new/docs/architecture/MCP_AND_OPENCLAW_INTEGRATION.md)** — Model Context Protocol (MCP 2024-11-05) JSON-RPC 2.0 endpoints, stdio bridge (`mcp_server.py`), and OpenClaw gateway specification.
- **[DEEP_RESEARCH_AND_EVIDENCE.md](file:///c:/Users/Suleman%20Agasimani/OneDrive/Desktop/Agentic/new/docs/architecture/DEEP_RESEARCH_AND_EVIDENCE.md)** — Google Search Grounding, pgvector dense embeddings, PostgreSQL full-text keyword retrieval, and Reciprocal Rank Fusion (RRF).
- **[CLOUD_SDLC_AND_DEPLOYMENT.md](file:///c:/Users/Suleman%20Agasimani/OneDrive/Desktop/Agentic/new/docs/architecture/CLOUD_SDLC_AND_DEPLOYMENT.md)** — Docker multi-stage builds, root `docker-compose.yml`, and GitHub Actions CI pipeline (`ci.yml`).

---

### 2. 🎓 Academic Peer Evaluation (`docs/evaluation/`)
Essential guides and grading matrices for academic peer evaluators:
- **[PEER_EVALUATION_CRITERIA_MATRIX.md](file:///c:/Users/Suleman%20Agasimani/OneDrive/Desktop/Agentic/new/docs/evaluation/PEER_EVALUATION_CRITERIA_MATRIX.md)** — Direct mapping of all 9 evaluation criteria (5 marks each = 45 marks total) to repository code, configuration, and verification commands.
- **[PEER_EVALUATION_DEMONSTRATION_GUIDE.md](file:///c:/Users/Suleman%20Agasimani/OneDrive/Desktop/Agentic/new/docs/evaluation/PEER_EVALUATION_DEMONSTRATION_GUIDE.md)** — Step-by-step 10-minute live evaluator demonstration runbook with expected outputs and verification checks.

---

### 3. 📊 Verification, Audit & Test Reports (`docs/reports/`)
Empirical validation logs, automated test results, and anti-hardcoding audits:
- **[FINAL_IMPLEMENTATION_AND_VERIFICATION_REPORT.md](file:///c:/Users/Suleman%20Agasimani/OneDrive/Desktop/Agentic/new/docs/reports/FINAL_IMPLEMENTATION_AND_VERIFICATION_REPORT.md)** — Comprehensive failover, PDF synthesis, and landing page audit.
- **[FIVE_AGENT_END_TO_END_VERIFICATION_REPORT.md](file:///c:/Users/Suleman%20Agasimani/OneDrive/Desktop/Agentic/new/docs/reports/FIVE_AGENT_END_TO_END_VERIFICATION_REPORT.md)** — Empirical end-to-end execution logs for all 5 specialist agents across multiple disciplines.
- **[LIVE_GEMINI_AND_EXTERNAL_RESEARCH_VERIFICATION.md](file:///c:/Users/Suleman%20Agasimani/OneDrive/Desktop/Agentic/new/docs/reports/LIVE_GEMINI_AND_EXTERNAL_RESEARCH_VERIFICATION.md)** — Live Google Gemini and Google Search Grounding verification report.
- **[LIVE_PROVIDER_AND_EXTERNAL_RESEARCH_REPORT.md](file:///c:/Users/Suleman%20Agasimani/OneDrive/Desktop/Agentic/new/docs/reports/LIVE_PROVIDER_AND_EXTERNAL_RESEARCH_REPORT.md)** — Dual-LLM (Groq + Gemini) runtime telemetry and latency benchmarks.
- **[MOCK_AND_FABRICATED_DATA_REMOVAL_REPORT.md](file:///c:/Users/Suleman%20Agasimani/OneDrive/Desktop/Agentic/new/docs/reports/MOCK_AND_FABRICATED_DATA_REMOVAL_REPORT.md)** — Proof of removal of fabricated responses and verification of dynamic generative execution.
- **[AGENT_REALITY_AND_HARDCODE_AUDIT_REPORT.md](file:///c:/Users/Suleman%20Agasimani/OneDrive/Desktop/Agentic/new/docs/reports/AGENT_REALITY_AND_HARDCODE_AUDIT_REPORT.md)** — Static code analysis ensuring genuine LLM prompting across all agents.
- **[COMPLETE_APPLICATION_IMPLEMENTATION_REPORT.md](file:///c:/Users/Suleman%20Agasimani/OneDrive/Desktop/Agentic/new/docs/reports/COMPLETE_APPLICATION_IMPLEMENTATION_REPORT.md)** — Full application capabilities baseline audit.
- **[PROJECT_CURRENT_STATE_AND_RECOVERY_REPORT.md](file:///c:/Users/Suleman%20Agasimani/OneDrive/Desktop/Agentic/new/docs/reports/PROJECT_CURRENT_STATE_AND_RECOVERY_REPORT.md)** — Historical architecture state and foundation recovery audit.

---

## 🔗 Quick Navigation

| Target Resource | Primary File |
| :--- | :--- |
| **Main Repository Overview** | [README.md](file:///c:/Users/Suleman%20Agasimani/OneDrive/Desktop/Agentic/new/README.md) |
| **System Architecture** | [ARCHITECTURE.md](file:///c:/Users/Suleman%20Agasimani/OneDrive/Desktop/Agentic/new/docs/architecture/ARCHITECTURE.md) |
| **Evaluation Rubric (45/45)** | [PEER_EVALUATION_CRITERIA_MATRIX.md](file:///c:/Users/Suleman%20Agasimani/OneDrive/Desktop/Agentic/new/docs/evaluation/PEER_EVALUATION_CRITERIA_MATRIX.md) |
| **Live Demo Script** | [PEER_EVALUATION_DEMONSTRATION_GUIDE.md](file:///c:/Users/Suleman%20Agasimani/OneDrive/Desktop/Agentic/new/docs/evaluation/PEER_EVALUATION_DEMONSTRATION_GUIDE.md) |
| **MCP Specification** | [MCP_AND_OPENCLAW_INTEGRATION.md](file:///c:/Users/Suleman%20Agasimani/OneDrive/Desktop/Agentic/new/docs/architecture/MCP_AND_OPENCLAW_INTEGRATION.md) |

