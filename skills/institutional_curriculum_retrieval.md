---
name: institutional_curriculum_retrieval
description: "Resolves accredited course codes, syllabus offerings, credit definitions, and prerequisites from the governed academic catalog"
category: "CATALOG_GOVERNANCE"
version: "1.0.0"
reusable_by: ["curriculum-mapping-agent", "academic-benchmarking-agent", "gap-analysis-agent", "mcp-gateway"]
---

# Institutional Curriculum Retrieval Skill

## 1. Purpose
Provides high-fidelity, deterministic resolution of institutional courses from the governed database. Prevents hallucination of nonexistent course codes or credit structures by anchoring proposals against actual university offerings.

## 2. Input Schema
```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "query": {
      "type": "string",
      "description": "Course title, topic keyword, or exact course code (e.g. 'CS302' or 'Machine Learning')"
    },
    "departmentId": {
      "type": "string",
      "description": "Optional UUID or department code to restrict lookup"
    },
    "programmeId": {
      "type": "string",
      "description": "Optional degree programme ID (UG/PG)"
    }
  },
  "required": ["query"]
}
```

## 3. Output Schema
```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "resolvedCourse": {
      "type": "object",
      "properties": {
        "id": { "type": "string" },
        "code": { "type": "string" },
        "title": { "type": "string" },
        "credits": { "type": "string" },
        "department": { "type": "string" }
      }
    },
    "resolutionStatus": {
      "type": "string",
      "enum": ["EXACT_MATCH", "RELATED_MATCH", "UNRECORDED_NEW_COURSE"]
    },
    "activeOfferings": { "type": "array" }
  },
  "required": ["resolutionStatus"]
}
```

## 4. Implementation & Tool Dependencies
- **Executed By:** `CurriculumMappingAgent.java`, `GovernedAcademicCatalogRetriever.java`
- **Underlying Governed Tool:** `academic_catalog_retrieval`
- **Database Tables:** `courses`, `departments`, `programmes`, `curriculum_versions`

## 5. Validation Rules & Fallback
- Exact course code pattern matching (`^[A-Z0-9]{2,6}\s?[0-9]{2,4}[A-Z0-9]?$`).
- If no course is found, safely mark status as `UNRECORDED_NEW_COURSE` and retrieve semantically related courses rather than fabricating catalog records.

## 6. Example Execution
```json
{
  "query": "Agentic AI",
  "departmentId": "dept-cse-ai"
}
```
**Output:**
```json
{
  "resolutionStatus": "UNRECORDED_NEW_COURSE",
  "resolvedCourse": null,
  "relatedCourses": [
    { "code": "26ECAC401", "title": "Deep Learning Systems", "credits": "3:0:2" }
  ]
}
```
