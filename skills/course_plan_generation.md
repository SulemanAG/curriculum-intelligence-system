---
name: course_plan_generation
description: "Dynamically synthesizes four distinct pedagogical course plan alternatives adhering to university credit conventions"
category: "PROPOSAL_SYNTHESIS"
version: "1.0.0"
reusable_by: ["recommendation-agent"]
---

# Course Plan Generation Skill

## 1. Purpose
Generates four distinct, non-trivial curricular alternatives reflecting genuine academic orientations:
1. **Industry-Aligned:** Enterprise tooling, production engineering, tool-calling frameworks.
2. **Academic Rigor:** Algorithmic formalisms, game theory, multi-agent proofs.
3. **Balanced Standard:** Institutional equilibrium between theory, credits, and laboratory experiments.
4. **Emerging Research:** Frontier neuro-symbolic methods, swarm self-organization.

## 2. Implementation
- **Executed By:** `RecommendationAgent.java`
- **Template Anchoring:** Adheres strictly to institutional credit formulas (L:T:P:C contact hours).
- **Anti-Hallucination:** Citation attribution is restricted exclusively to the upstream `allowedCitations` list.
