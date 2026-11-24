# Peer Evaluation Demonstration Guide
## Step-by-Step Evaluator Walkthrough (KLE Technological University — 45 Marks)

---

## 1. Evaluator Quickstart (Under 3 Minutes)

The evaluator can inspect the working system either via the **Web UI** or via **CLI / curl commands**.

### Prerequisites Check
- PostgreSQL 16/18 running on port `5432` with database `curriculum_intelligence`.
- Spring Boot backend running on port `8081`.
- React frontend running on port `5173`.

---

## 2. Interactive Web UI Demonstration

### Stage 1: Landing Page
1. Open browser to `http://localhost:5173/`.
2. Observe the clean institutional landing page with value propositions, five specialist agents overview, and accreditation compliance features.
3. Click the **"Get Started"** or **"Launch AI Workspace"** button.

### Stage 2: Conversational Curriculum Design Request
1. In the chat composer at the bottom, enter:
   > *"Design an undergraduate course on Agentic AI and Autonomous Systems for 6th semester Computer Science students."*
2. Press **Enter** or click the send button.

### Stage 3: Real-Time Multi-Agent Progress Tracking
1. The UI displays the live **AURA Orchestration Stage Card**:
   - **Stage 1: Intake & Clarification** (`completed`)
   - **Stage 2: Parallel View-Building** (`running` $\rightarrow$ `completed`):
     - *Curriculum Mapping Agent:* Resolves accredited course catalog and prerequisites.
     - *Industry Skills Agent:* Performs external Google Search Grounding for live tech stacks.
     - *Academic Benchmarking Agent:* Surveys peer syllabi (ACM/IEEE CS2023).
   - **Stage 3: Curriculum Gap Analysis** (`completed`): Synthesizes upstream findings and computes Curriculum Health Score.
   - **Stage 4: Proposal Recommendation** (`completed`): Formulates four distinct proposals.
2. Note the header badge: Shows active provider (`Groq ⇄ Gemini`). If Groq encounters rate limiting, the badge dynamically reflects failover to Gemini.

### Stage 4: Reviewing the Four Curricular Alternatives
1. The system presents **Four Distinct Alternatives**:
   - `Alternative 1`: **Industry-Aligned Autonomous Systems** (Orientation: `INDUSTRY`, 3:0:2)
   - `Alternative 2`: **Academic Rigor & Theoretical Foundation** (Orientation: `ACADEMIC_DEPTH`, 4:0:0)
   - `Alternative 3`: **Balanced Institutional Standard** (Orientation: `BALANCED`, 3:1:0)
   - `Alternative 4`: **Emerging Research & Frontiers** (Orientation: `EMERGING_RESEARCH`, 3:0:2)
2. Expand the accordion to inspect Course Units, Lab Experiments, and Reference Textbooks.

### Stage 5: Faculty Review & Document Download
1. Select an alternative (e.g. Alternative 1).
2. Enter faculty remarks/rationale in the feedback modal:
   > *"Approved for Board of Studies review. Strong practical tooling focus."*
3. Click **Submit Feedback**.
4. The **Final Document Stage Card** appears:
   - Click **"Download Official DOCX"** $\rightarrow$ Downloads the populated Word document using the master KLE Tech template.
   - Click **"Download Official PDF"** $\rightarrow$ Downloads the compiled 24-page PDF document.

---

## 3. Command-Line / API Demonstration (curl)

If the evaluator prefers terminal inspection:

### 1. Check LLM Runtime & Failover Status
```bash
curl -s http://localhost:8081/api/curriculum/llm/status
```
*Returns active provider, circuit breaker state, and failover telemetry without exposing API keys.*

### 2. Inspect Model Context Protocol (MCP) Server
```bash
# Discover tools via MCP protocol
curl -s -X POST http://localhost:8081/api/mcp \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":1,"method":"tools/list","params":{}}'

# Call a tool over MCP (e.g. academic catalog retrieval)
curl -s -X POST http://localhost:8081/api/mcp \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":2,"method":"tools/call","params":{"name":"academic_catalog_retrieval","arguments":{"query":"Deep Learning"}}}'
```

### 3. Check OpenCLA / OpenClaw Integration Status
```bash
curl -s http://localhost:8081/api/curriculum/openclaw/status
```
*Returns honest `STANDBY` status verifying that AURA operates natively in autonomous mode.*

### 4. Submit Course Request to AURA Coordinator
```bash
curl -s -X POST http://localhost:8081/api/curriculum/requests \
  -H "Content-Type: application/json" \
  -d '{"rawPrompt":"Design an undergraduate course on Agentic AI and Autonomous Systems for 6th semester Computer Science students.","department":"School of Computer Science & Engineering (AI)","degreeLevel":"UG"}'
```
*Returns run ID (e.g. `run-xxxx-xxxx`).*

### 5. Retrieve Synthesized Alternatives
```bash
curl -s http://localhost:8081/api/curriculum/runs/{runId}/alternatives
```

### 6. Verify Document Generation
```bash
# Download generated Word Document
curl -s -o Course_Plan.docx http://localhost:8081/api/curriculum/plans/{planId}/docx

# Download generated PDF
curl -s -o Course_Plan.pdf http://localhost:8081/api/curriculum/plans/{planId}/pdf
```

---

## 4. Key Verification Checkpoints for Evaluator

1. **Anti-Hallucination:** Inspect citations in the alternatives; each maps to an actual URL in `discovered_sources`.
2. **Template Preservation:** Open `Course_Plan.docx`; notice that the KLE Technological University logo, margins, and header tables are identical to the blank master template.
3. **No Fake Stubs:** Check terminal logs of `curriculum-intelligence-backend`; see real thread execution and LLM responses.
