# Deep Research & Evidence Grounding Engine
## Multi-Modal Retrieval, Vector Embeddings, and Anti-Hallucination Pipeline

---

## 1. Research Engine Architecture

The **Curriculum Intelligence System** employs a strict evidence-driven paradigm. No specialist agent is permitted to formulate curricular claims or recommend syllabus topics based on ungrounded model training weights alone.

```
                  [Specialist Research Request]
                               │
                               ▼
                [Google Search Grounding Service]
                               │
            ┌──────────────────┴──────────────────┐
            ▼                                     ▼
     [Academic Standards]                [Industry Competencies]
     - ACM CS2023 Guidelines            - Live Tech Stack Demand
     - IEEE Computer Society             - Framework Ecosystems
     - IIT / NIT / MIT Syllabi           - Production Engineering Skills
            │                                     │
            └──────────────────┬──────────────────┘
                               │
                               ▼
                  [Discovered Sources Registry]
                  (URL, Domain, Publisher, Title)
                               │
                               ▼
                   [Evidence Chunking Engine]
                   (Token Splitting & Provenance)
                               │
                               ▼
                 [Jina AI Vector Embeddings]
                 (jina-embeddings-v2-base-en)
                               │
                               ▼
               [PostgreSQL pgvector / Full-Text]
                               │
                               ▼
             [Reciprocal Rank Fusion (RRF) Hybrid Search]
                               │
                               ▼
                 [Agent Reasoning with Citations]
```

---

## 2. Ingestion & Provenance Tracking

When `ExternalResearchService` executes:
1. **Google Grounding Execution:** A dynamic search query is dispatched (e.g. `undergraduate syllabus curriculum course outline Agentic AI and Autonomous Systems peer universities accreditation standards ACM IEEE`).
2. **Source Registration:** Discovered web pages are registered in `discovered_sources` table with:
   - `id`: UUID (e.g. `src-a0b21682`)
   - `title`: Web page title
   - `url`: Canonical source URL
   - `domain`: Source domain (e.g. `acm.org`, `ieee.org`, `github.com`)
   - `retrieval_timestamp`: Exact ISO 8601 timestamp
3. **Evidence Chunking:** Extracted body text is normalized and segmented into 500-token chunks with 50-token overlaps, assigned unique chunk IDs (`chk-xxxx`), and stored in `evidence_chunks`.
4. **Vector Embedding:** High-dimensional vector representations (768 dimensions) are computed via Jina AI (`jina-embeddings-v2-base-en`) and stored in the PostgreSQL database.

---

## 3. Reciprocal Rank Fusion (RRF) Hybrid Retrieval

To overcome the blind spots of keyword search (vocabulary mismatch) and dense vector search (keyword specificity loss), the system implements **Reciprocal Rank Fusion (RRF)**:

$$RRF\_Score(d) = \sum_{m \in M} \frac{1}{k + r_m(d)}$$

Where:
- $M = \{\text{Lexical Full-Text}, \text{Dense Vector Semantic}\}$
- $k = 60$ (smoothing constant)
- $r_m(d) =$ rank position of document $d$ within retrieval modality $m$.

### Resilience Fallback
If the vector embedding API is unconfigured or experiences network downtime:
- `evidence_semantic_search` cleanly reports `modality: SEMANTIC_VECTOR, status: UNAVAILABLE`.
- The hybrid search service gracefully degrades to lexical full-text scoring, ensuring the multi-agent pipeline never halts.

---

## 4. Citation Gating & Anti-Fabrication Rules

The system enforces strict anti-hallucination guardrails:
1. **Upstream Permitted Citations:** Research agents build an immutable `allowedSourceUrls` whitelist.
2. **Prompt Boundary:** The reasoning prompt explicitly instructs the LLM:
   > *"Every important factual claim, reference textbook, or benchmark comparison must strictly cite items from the ALLOWED CITATIONS list. Do not fabricate research papers or non-existent URLs."*
3. **Downstream Validation:** The `RecommendationAgent` and `KleTechDocumentService` verify that all generated textbook references and peer institution comparisons map directly to verified evidence chunks in the database.
