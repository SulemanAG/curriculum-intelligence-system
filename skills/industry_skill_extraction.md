---
name: industry_skill_extraction
description: "Mines live labor market competency demand, industry frameworks, and software stacks using Google Search Grounding"
category: "MARKET_INTELLIGENCE"
version: "1.0.0"
reusable_by: ["industry-skills-agent", "gap-analysis-agent", "mcp-gateway"]
---

# Industry Skill Extraction Skill

## 1. Purpose
Gathers verifiable industry hiring requirements, emerging developer tooling, and enterprise competency models from live market data.

## 2. Input Schema
```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "subject": { "type": "string", "description": "Course domain or target technology" },
    "targetIndustrySectors": { "type": "array", "items": { "type": "string" } },
    "maxEvidenceSources": { "type": "integer", "default": 5 }
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
    "marketSummary": { "type": "string" },
    "topFrameworks": { "type": "array", "items": { "type": "string" } },
    "requiredCompetencies": { "type": "array" },
    "evidenceCitations": { "type": "array", "items": { "type": "string" } },
    "limitations": { "type": "array", "items": { "type": "string" } }
  },
  "required": ["subject", "topFrameworks", "evidenceCitations"]
}
```

## 4. Implementation & Tool Dependencies
- **Executed By:** `IndustrySkillsAgent.java`, `ExternalResearchService.java`
- **Underlying Tools:** `evidence_hybrid_search`, `evidence_keyword_search`
- **External API:** Google Search Grounding (gemini-2.5-flash with dynamic web search tool)

## 5. Validation Rules & Anti-Fabrication
- Every cited URL or domain must originate from the verified `allowedUrls` array returned by search grounding.
- If web retrieval yields empty results, the agent enters `BLOCKED` state with explicit failure reason rather than generating generic skills.
