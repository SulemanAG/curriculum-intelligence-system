---
name: academic_syllabus_benchmarking
description: "Surveys tier-1 peer institutions (IITs, NITs, MIT, Stanford) and ACM/IEEE CS Curricula standards"
category: "ACADEMIC_EXCELLENCE"
version: "1.0.0"
reusable_by: ["academic-benchmarking-agent", "gap-analysis-agent", "mcp-gateway"]
---

# Academic Syllabus Benchmarking Skill

## 1. Purpose
Extracts syllabus outlines, credit distributions, and theoretical rigor benchmarks from accredited peer institutions and professional curricular standards (ACM CS2023, IEEE Computer Society).

## 2. Input Schema
```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "subject": { "type": "string" },
    "benchmarkInstitutions": { "type": "array", "items": { "type": "string" } },
    "accreditationStandards": { "type": "array", "items": { "type": "string" } }
  },
  "required": ["subject"]
}
```

## 3. Output Schema
```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "subject": { "type": "string" },
    "standardAlignments": { "type": "array" },
    "peerOfferings": { "type": "array" },
    "evidenceCitations": { "type": "array", "items": { "type": "string" } }
  },
  "required": ["subject", "peerOfferings", "evidenceCitations"]
}
```

## 4. Implementation & Tool Dependencies
- **Executed By:** `AcademicBenchmarkingAgent.java`, `ExternalResearchService.java`
- **Underlying Tools:** `academic_catalog_retrieval`, `evidence_hybrid_search`
- **Storage:** Persisted in `discovered_sources`, `evidence_documents`, `evidence_chunks`
