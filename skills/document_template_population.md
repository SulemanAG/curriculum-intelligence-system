---
name: document_template_population
description: "Populates the official KLE Technological University Course Plan master DOCX template preserving original layout and styles"
category: "DOCUMENT_ENGINEERING"
version: "1.0.0"
reusable_by: ["coordinator-service", "document-service"]
---

# Document Template Population Skill

## 1. Purpose
Populates `KLE_Tech_Course_Plan_Blank_Template_No_Marks_Scheme.docx` with selected course plan data, injecting course outcomes, unit outlines, laboratory exercises, and textbooks while strictly preserving headers, footers, logo dimensions, table widths, and font hierarchies.

## 2. Implementation
- **Executed By:** `KleTechDocumentService.java`
- **Underlying Library:** Apache POI (`XWPFDocument`, `XWPFTable`, `XWPFParagraph`)
- **Key Invariants:**
  - Preserves exact 5-column unit tables and 4-column experiment tables.
  - Zero XML placeholder leakage (e.g. `{{COURSE_CODE}}` fully resolved).
  - Clean table row clonation preventing style divergence.
