# Curriculum AI — Conversational AI Workspace (Frontend Redesign)
**KLE Technological University, Belagavi**  
**School of Computer Science & Engineering (AI)**  
**Course:** Agentic AI (26ECAC401)  
**Reference Document:** Chapter 5 (Curriculum Intelligence System), Agentic AI Worklets 14-Lab Build Book

---

## 1. Overview & Conversational Redesign

The **Curriculum Intelligence System** frontend has been redesigned into a modern conversational AI application inspired by the interaction patterns and visual simplicity of **ChatGPT** and **Claude**.

### Visual & Interactive Philosophy
- **Predominantly White & Light-Blue Palette:** Main conversation workspace in crisp white, persistent sidebar in very light blue-grey (`#f8fafc`), dark navy/slate typography (`text-slate-900`), and restrained professional blue accents.
- **Conversational Workflow:** University faculty interact through natural conversation (e.g., *"I need to design a course plan for Computer Vision."*).
- **Embedded Agentic Responses:** Research stages, parallel swarm view-building, four alternative proposals, evidence grounding, faculty review, and final institutional documents are rendered naturally inside the conversational message stream.
- **Zero Technical Overhead:** Faculty members are not exposed to technical orchestration graphs or raw JSON, while maintaining full source transparency and audit compliance.

---

## 2. Application Layout & Component Architecture

### Layout Structure
```
src/
├── components/
│   ├── layout/
│   │   ├── Sidebar.tsx                  # Collapsible left sidebar with navigation & chat history
│   │   └── ChatHeader.tsx               # Top workspace header with conversation title & actions menu
│   ├── chat/
│   │   ├── ChatWorkspace.tsx            # Main conversation stream & composer container
│   │   ├── ChatMessageItem.tsx          # User (right-aligned) & Assistant (left-aligned) bubbles
│   │   ├── MessageComposer.tsx          # Auto-growing multiline composer with Enter-to-send
│   │   ├── WelcomeScreen.tsx            # Claude/ChatGPT-style welcome screen with prompt cards
│   │   └── ConversationalResearchStage.tsx # Compact in-chat research progress & parallel swarm
│   ├── AlternativeComparison.tsx        # 4-alternative side-by-side & matrix comparison
│   ├── AlternativePlanCard.tsx          # Individual proposal card with hours, credits & grounding %
│   ├── CoursePlanDetails.tsx            # Modal view with CO-PO maps, modules & evidence
│   ├── ClarificationDialog.tsx          # Lab 1 constraint planner dialog
│   ├── FeedbackForm.tsx                 # Human review gate (Lab 12) & preference recorder
│   ├── FinalDocumentPreview.tsx         # Template-driven Course Instruction Plan
│   └── PdfDownloadAction.tsx            # PDF export action with honest network states
├── pages/
│   ├── AnalyzeAgentsPage.tsx            # Agent telemetry placeholder (Phase 2 capability)
│   ├── LibraryPage.tsx                  # Document library for generated course plan PDFs
│   ├── ScheduledPage.tsx                # Recurring scheduled scans & honest empty state
│   ├── OtherToolsPage.tsx               # Modular academic tool integrations placeholder
│   └── ProfilePage.tsx                  # User profile, department, and backend settings
├── data/
│   └── schemaPreviewData.ts             # Watermarked Chapter 5 schema preview records
├── services/
│   └── api.ts                           # Proposed integration contracts for Spring Boot backend
└── types/
    └── index.ts                         # Strongly typed TypeScript interfaces
```

---

## 3. Sidebar Navigation Specifications

The persistent left sidebar includes:
1. **Header:** Application logo & brand name: **Curriculum AI**.
2. **Action:** Prominent **+ New Chat** button.
3. **Navigation Items (in precise required order):**
   1. **Analyze Agents:** Telemetry & multi-agent orchestration inspection (Phase 2 placeholder).
   2. **Library:** Archive of finalized course plan PDFs with search and download actions.
   3. **Scheduled:** Scheduled curriculum scans & cron tasks with honest empty states.
   4. **Other Tools:** Extensible directory for future departmental utilities.
4. **Chat History:** List of previous conversations with auto-generated titles, active blue highlighting (`bg-blue-50 text-brand-900`), and session persistence.
5. **Anchored Profile Section:** Fixed at bottom with faculty avatar (`FM`), display name, department, and settings shortcut.

---

## 4. Real Data Policy Adherence

- **No Simulated Data as Real:** The application strictly enforces the real-data policy.
- When backend services are offline, the interface displays an honest status indicator (`Standalone Mode`).
- Any schema preview records are explicitly watermarked: `[DEVELOPMENT ONLY SCHEMA PREVIEW: Demonstrating Chapter 5 Data Model. NOT real research results]`.

---

## 5. Development & Verification Commands

```powershell
# 1. Install dependencies
npm install

# 2. Start Vite development server (port 5173)
npm run dev

# 3. Production build & TypeScript strict check
npm run build

# 4. Preview production build locally
npm run preview
```
