---
name: competency_gap_identification
description: "Compares institutional baseline against industry market demand and academic benchmarks to identify curricular deficits"
category: "EVALUATION_SYNTHESIS"
version: "1.0.0"
reusable_by: ["gap-analysis-agent", "recommendation-agent"]
---

# Competency Gap Identification Skill

## 1. Purpose
Synthesizes findings from upstream research agents to systematically detect outdated content, missing modern topics, prerequisite deficiencies, and laboratory imbalances.

## 2. Input Schema
```json
{
  "subject": "string",
  "institutionalBaseline": "object",
  "industryCompetencies": "object",
  "academicBenchmarks": "object"
}
```

## 3. Output Schema
```json
{
  "curriculumHealthScore": "integer (0-100)",
  "identifiedGaps": [
    {
      "gapId": "string",
      "category": "MISSING_TOPIC | OBSOLETE_CONTENT | DEPTH_DEFICIT | LAB_PRACTICAL | PREREQUISITE",
      "title": "string",
      "severity": "CRITICAL | HIGH | MEDIUM | LOW",
      "remediationRecommendation": "string"
    }
  ]
}
```

## 4. Implementation
- **Executed By:** `GapAnalysisAgent.java`
- **Pre-condition:** Requires valid completed task outputs from all three upstream parallel agents (`task_curriculum_mapping`, `task_industry_skills`, `task_academic_benchmarking`).
- **Resilience:** If upstream outputs are incomplete, emits honest `BLOCKED` status with explicit upstream missing requirements.
