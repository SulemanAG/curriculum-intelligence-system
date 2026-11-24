# Specialist Agent & Reusable Skill Specifications

---

## Part 1: The Five Specialist Agents

### 1. Curriculum Mapping Agent
- **ID:** `curriculum-mapping-agent`
- **Class:** `org.curriculumai.orchestration.agent.curriculum.CurriculumMappingAgent`
- **Objective:** Anchors course design in accredited university structures, degree regulations, and Bloom's taxonomy.
- **Permitted Tools:** `academic_catalog_retrieval`, `curriculum_version_retrieval`
- **Execution Mode:** Parallel (Worker 1)
- **Input:** Subject title, target department, degree level (UG/PG).
- **Output:** Institutional course code, title, credit distribution (L:T:P:C), Bloom-mapped Course Outcomes (CO1–CO4), and prerequisite graph.

### 2. Industry Skills Agent
- **ID:** `industry-skills-agent`
- **Class:** `org.curriculumai.orchestration.agent.industry.IndustrySkillsAgent`
- **Objective:** Mines live labor market demand, tech stack trends, and enterprise hiring expectations.
- **Permitted Tools:** `evidence_hybrid_search`, `evidence_keyword_search`
- **External Integration:** Google Search Grounding (`ExternalResearchService`)
- **Execution Mode:** Parallel (Worker 2)
- **Input:** Course domain keyword (e.g. "Agentic AI and Autonomous Systems").
- **Output:** Categorized framework rankings (e.g. LangGraph, CrewAI, AutoGen), production skills, and verified citation references.

### 3. Academic Benchmarking Agent
- **ID:** `academic-benchmarking-agent`
- **Class:** `org.curriculumai.orchestration.agent.benchmarking.AcademicBenchmarkingAgent`
- **Objective:** Cross-references curriculum content with tier-1 institutions (IIT Bombay, IIT Madras, MIT, Stanford) and ACM/IEEE CS2023 curricula.
- **Permitted Tools:** `academic_catalog_retrieval`, `evidence_hybrid_search`, `evidence_keyword_search`
- **External Integration:** Google Search Grounding (`ExternalResearchService`)
- **Execution Mode:** Parallel (Worker 3)
- **Input:** Course domain and academic standards search parameters.
- **Output:** Peer institutional syllabi models, credit benchmarks, and textbook standards.

### 4. Gap Analysis Agent
- **ID:** `gap-analysis-agent`
- **Class:** `org.curriculumai.orchestration.agent.gap.GapAnalysisAgent`
- **Objective:** Synthesizes institutional baseline, industry skills, and academic benchmarks to identify curricular deficits.
- **Execution Mode:** Sequential (Dispatched after Tasks 1, 2, and 3 complete)
- **Dependency Invariant:** Validates presence and completeness of all 3 upstream task outputs. Halts with `BLOCKED` status if any upstream output is absent.
- **Output:** Curricular health score (0–100), categorized gaps (`MISSING_TOPIC`, `OBSOLETE_CONTENT`, `DEPTH_DEFICIT`, `LAB_PRACTICAL`, `PREREQUISITE`), and remediation actions.

### 5. Recommendation Agent
- **ID:** `recommendation-agent`
- **Class:** `org.curriculumai.orchestration.agent.recommendation.RecommendationAgent`
- **Objective:** Formulates 4 distinct curricular proposals and an evaluation matrix based strictly on verified research.
- **Execution Mode:** Sequential (Dispatched after Gap Analysis completes)
- **Four Orientations:**
  1. `INDUSTRY`: Focus on production software frameworks and hands-on tooling.
  2. `ACADEMIC_DEPTH`: Emphasis on mathematical proofs, algorithms, and formal foundations.
  3. `BALANCED`: Standard institutional alignment balancing theory and practical contact hours.
  4. `EMERGING_RESEARCH`: Cutting-edge frontier topics and open research questions.
- **Output:** Exactly 4 course alternatives with unit breakdowns, lab experiments, reference textbooks, and comparison matrix.

---

## Part 2: Reusable Agent Skills Catalog

| Skill Name | Category | Primary Agents | Underlying Tools |
| :--- | :--- | :--- | :--- |
| **`institutional_curriculum_retrieval`** | Catalog Governance | Curriculum Mapping, Academic Benchmarking | `academic_catalog_retrieval` |
| **`course_outcome_analysis`** | Academic Governance | Curriculum Mapping | Institutional Bloom Engine |
| **`industry_skill_extraction`** | Market Intelligence | Industry Skills, Gap Analysis | `evidence_hybrid_search`, Search Grounding |
| **`academic_syllabus_benchmarking`** | Academic Excellence | Academic Benchmarking | `evidence_keyword_search`, Search Grounding |
| **`evidence_verification`** | Content Trust | All Research Agents | `evidence_documents`, Chunk Validator |
| **`competency_gap_identification`** | Evaluation Synthesis | Gap Analysis | Multi-Perspective Comparator |
| **`co_po_mapping`** | Accreditation Rigor | Curriculum Mapping, Recommendation | Institutional PO Matrix (PO1-PO12) |
| **`course_plan_generation`** | Proposal Synthesis | Recommendation Agent | Dynamic Synthesis & Citation Gating |
| **`citation_validation`** | Anti-Hallucination | Recommendation Agent | Upstream Allowed Citations Filter |
| **`document_template_population`** | Document Engineering | Coordinator, Document Service | Apache POI Master Template Engine |
| **`pdf_validation`** | Quality Assurance | Document Service | PDFBox / Word Headless Verifier |

---

## Part 3: Skill Specifications & Examples

### Skill: `citation_validation`
- **Purpose:** Enforces zero-tolerance against hallucinated external citations.
- **Rule:** Every citation string, textbook reference, or URL in synthesized alternatives must match an element from the upstream `allowedCitations` list compiled during research.
- **Failure Behavior:** Any fabricated link is stripped before final proposal generation.

### Skill: `co_po_mapping`
- **Purpose:** Computes correlation matrices between proposed Course Outcomes (COs) and NBA/ABET Program Outcomes (PO1: Engineering Knowledge, PO2: Problem Analysis, PO3: Design/Development of Solutions, etc.).
- **Scoring:** Assigns correlation weights (1: Low, 2: Medium, 3: High) grounded in Bloom taxonomy levels.
