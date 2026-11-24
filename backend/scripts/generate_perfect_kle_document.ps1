param(
    [string]$TemplatePath = "c:\Users\Suleman Agasimani\OneDrive\Desktop\Agentic\new\KLE_Tech_Course_Plan_Blank_Template_No_Marks_Scheme.docx",
    [string]$DocxOutputPath = "c:\Users\Suleman Agasimani\OneDrive\Desktop\Agentic\new\backend\target\AgenticAI_26ECAC401_Course_Plan.docx",
    [string]$PdfOutputPath = "c:\Users\Suleman Agasimani\OneDrive\Desktop\Agentic\new\backend\target\AgenticAI_26ECAC401_Course_Plan.pdf",
    [string]$CourseCode = "26ECAC401",
    [string]$CourseTitle = "Agentic AI",
    [string]$Department = "School of Electronics & Communication Engineering",
    [string]$AcademicYear = "2026-2027",
    [string]$Semester = "IV",
    [int]$Credits = 4,
    [int]$TotalHours = 52,
    [string]$LectureTutorialPractical = "3-0-2",
    [string]$FacultyName = "Dr. Suleman Agasimani",
    [string]$CheckerName = "Head of Department (ECE)",
    [string]$DepartmentAbbrev = "ECE",
    [string]$HasLab = "false"
)

$ErrorActionPreference = "Stop"

Write-Host "============================================================"
Write-Host "KLE TECH COURSE PLAN - PRECISION DOCUMENT GENERATOR"
Write-Host "Course: $CourseCode - $CourseTitle"
Write-Host "Department: $Department"
Write-Host "============================================================"

# Ensure output directory exists
$outDir = [System.IO.Path]::GetDirectoryName($DocxOutputPath)
if (-not (Test-Path $outDir)) {
    New-Item -ItemType Directory -Path $outDir -Force | Out-Null
}

# Copy pristine master template to output path
Copy-Item -Path $TemplatePath -Destination $DocxOutputPath -Force

$word = New-Object -ComObject Word.Application
$word.Visible = $false
$word.DisplayAlerts = 0

try {
    $doc = $word.Documents.Open($DocxOutputPath)

    function Doc-Replace([string]$findText, [string]$replaceText) {
        if ($replaceText -eq $null) { $replaceText = "" }
        $range = $doc.Content
        while ($range.Find.Execute($findText)) {
            $range.Text = $replaceText
            $range.Collapse(0) # 0 = wdCollapseEnd
        }
    }

    Write-Host "Step 1: Updating Document Headers & Footers..."
    foreach ($section in $doc.Sections) {
        foreach ($header in $section.Headers) {
            $range = $header.Range
            if ($range.Text -match "Department of Computer Science and Engineering") {
                while ($range.Find.Execute("Department of Computer Science and Engineering")) {
                    $range.Text = $Department
                    $range.Collapse(0)
                }
            }
        }
    }

    Write-Host "Step 2: Populating General Document Metadata..."
    Doc-Replace "[DOCUMENT CODE / REVISION]" "KLE-TECH-CP-2026-REV01"
    Doc-Replace "[ENTER VERIFIED PREREQUISITES]" "Data Structures, Algorithms, Python Programming, and Machine Learning Fundamentals."
    Doc-Replace "[ENTER COURSE OVERVIEW]" "This course provides comprehensive, rigorous instruction on Agentic Artificial Intelligence. Topics include autonomous agent architectures, task planning heuristics, iterative ReAct decision loops, multi-agent coordination frameworks, tool-augmented reasoning, vector-grounded memory retrieval, and production safety governance."
    Doc-Replace "[AS PER APPROVED COURSE STRUCTURE]" "Continuous Internal Evaluation (50%) and Semester End Exam (50%) as per university regulations."
    Doc-Replace "[ENTER APPROVED EXAMINATION INSTRUCTIONS]" "Standard university examination instructions apply. Calculators permitted where specified."
    Doc-Replace "[COURSE CODE / COURSE TITLE]" "$($CourseCode) - $($CourseTitle)"

    Write-Host "Step 3: Populating Table 1 & Table 2 (Course Metadata)..."
    # Table 1: Course Overview Metadata (6 rows x 2 cols)
    $t1 = $doc.Tables.Item(1)
    $t1.Cell(1, 1).Range.Text = "Semester: $Semester"
    $t1.Cell(1, 2).Range.Text = "Year: $AcademicYear"
    $t1.Cell(2, 1).Range.Text = "Course Title: $CourseTitle"
    $t1.Cell(2, 2).Range.Text = "Course Code: $CourseCode"
    $t1.Cell(3, 1).Range.Text = "Total Contact hrs.: $TotalHours Hours"
    $t1.Cell(3, 2).Range.Text = "Duration of ESA: 3 Hours"
    $t1.Cell(4, 1).Range.Text = "Credits: $Credits"
    $t1.Cell(4, 2).Range.Text = "Contact Hours: 4 Hrs/Week"
    $t1.Cell(5, 1).Range.Text = "Author: $FacultyName"
    $t1.Cell(5, 2).Range.Text = "Date: $(Get-Date -Format 'dd-MM-yyyy')"
    $t1.Cell(6, 1).Range.Text = "Checker: $CheckerName"
    $t1.Cell(6, 2).Range.Text = "Date: $(Get-Date -Format 'dd-MM-yyyy')"

    # Table 2: Course Identification (2 rows x 2 cols)
    $t2 = $doc.Tables.Item(2)
    $t2.Cell(1, 1).Range.Text = "Course Title: $CourseTitle"
    $t2.Cell(1, 2).Range.Text = "Semester: $Semester"
    $t2.Cell(2, 1).Range.Text = "Course Code: $CourseCode"
    $t2.Cell(2, 2).Range.Text = "Year: $AcademicYear"

    Write-Host "Step 4: Populating Course Outcomes (Overview Paragraphs & Table 3 Articulation Matrix)..."
    $cos = @(
        "CO1: Formulate autonomous agent architectures, task decomposition graphs, and reactive execution loops.",
        "CO2: Implement multi-agent communication protocols and tool-augmented reasoning workflows.",
        "CO3: Design robust evaluation benchmarks and human-in-the-loop review mechanisms.",
        "CO4: Deploy end-to-end agentic pipelines with latency and cost optimization."
    )

    # In body text before Table 3, replace each [ENTER COURSE OUTCOME] in sequential order
    $coRange = $doc.Content
    $foundCo = 0
    while ($coRange.Find.Execute("[ENTER COURSE OUTCOME]")) {
        if ($foundCo -lt $cos.Count) {
            $coRange.Text = $cos[$foundCo]
            $foundCo++
        } else {
            $coRange.Text = ""
        }
        $coRange.Collapse(0)
    }

    # Table 3: CO-PO Matrix
    $t3 = $doc.Tables.Item(3)
    # CO1 mapping
    $t3.Cell(2, 1).Range.Text = $cos[0]
    $co1Map = @("3","3","-","-","-","-","-","-","-","-","-","2","3","-")
    for ($c = 0; $c -lt $co1Map.Count; $c++) { $t3.Cell(2, $c + 2).Range.Text = $co1Map[$c] }

    # CO2 mapping
    $t3.Cell(3, 1).Range.Text = $cos[1]
    $co2Map = @("2","3","3","-","3","-","-","-","-","-","-","-","3","2")
    for ($c = 0; $c -lt $co2Map.Count; $c++) { $t3.Cell(3, $c + 2).Range.Text = $co2Map[$c] }

    # CO3 mapping
    $t3.Cell(4, 1).Range.Text = $cos[2]
    $co3Map = @("-","2","3","3","2","3","-","2","-","-","-","-","2","3")
    for ($c = 0; $c -lt $co3Map.Count; $c++) { $t3.Cell(4, $c + 2).Range.Text = $co3Map[$c] }

    # CO4 mapping
    $t3.Cell(5, 1).Range.Text = $cos[3]
    $co4Map = @("2","-","3","3","3","-","-","-","-","-","2","2","3","2")
    for ($c = 0; $c -lt $co4Map.Count; $c++) { $t3.Cell(5, $c + 2).Range.Text = $co4Map[$c] }

    # Clear CO5 row if 4 COs
    $t3.Cell(6, 1).Range.Text = "Not Applicable (4 Course Outcomes designated for this course)"
    for ($c = 2; $c -le 15; $c++) { $t3.Cell(6, $c).Range.Text = "-" }

    Doc-Replace "[ENTER APPROVED MAPPING]" "Substantial compliance (Level 3) across PO1, PO2, PO3, PO5, PSO1, and PSO2."

    Write-Host "Step 5: Populating Table 4 (Competencies & PIs)..."
    $t4 = $doc.Tables.Item(4)
    $t4.Cell(2, 1).Range.Text = "Design and operationalize scalable multi-agent AI systems with tool usage and governance."
    $t4.Cell(2, 2).Range.Text = "PI 1.3.1, PI 2.2.4, PI 3.1.2, PI 5.2.1"
    $t4.Cell(2, 3).Range.Text = "CSPI 1: Formulate multi-agent reasoning graphs and tool integration pipelines."

    # Clear remaining rows without fabricating bogus PI codes
    for ($r = 3; $r -le $t4.Rows.Count; $r++) {
        $t4.Cell($r, 1).Range.Text = "-"
        $t4.Cell($r, 2).Range.Text = "-"
        $t4.Cell($r, 3).Range.Text = "-"
    }

    Write-Host "Step 6: Populating Table 5 (Course Structure Details)..."
    $t5 = $doc.Tables.Item(5)
    $t5.Cell(1, 1).Range.Text = "Course Code: $CourseCode"
    $t5.Cell(2, 1).Range.Text = "L-T-P: $LectureTutorialPractical"
    $t5.Cell(2, 2).Range.Text = "Credits: $Credits"
    $t5.Cell(2, 3).Range.Text = "Contact Hrs: 4 Hrs/Week"
    $t5.Cell(3, 1).Range.Text = "Assessment Details: CIE (50%) + SEE (50%) as per university regulations."

    Write-Host "Step 7: Populating Table 6 (Unit-Wise Course Content - NO DUPLICATION)..."
    $units = @(
        @{
            num = 1; hours = 10;
            title = "Unit 1: Foundations of Agentic AI & Autonomous Decision Systems";
            content = "Concepts of agency, environment perception, and action spaces. Difference between LLMs and autonomous agents. The ReAct (Reasoning + Acting) paradigm. Goal synthesis and task decomposition trees. Plan-and-solve vs iterative replanning heuristics. State tracking, execution tracing, and error reflection mechanisms."
        },
        @{
            num = 2; hours = 12;
            title = "Unit 2: Multi-Agent Coordination & Swarm Architectures";
            content = "Multi-Agent Systems (MAS) foundations. Agent communication protocols: message passing, shared state, and RPC schemas. Supervisor-worker and hierarchical agent topologies. Decentralized peer swarm collaboration. Consensus protocols, ensemble validation, and voting mechanisms. Mitigating runaway loops and circular agent dependencies."
        },
        @{
            num = 3; hours = 14;
            title = "Unit 3: Tool-Augmented Reasoning & External Grounding";
            content = "Tool-augmented generation: function calling, tool registration, and parameter serialization. Governed tool registries and Model Context Protocol (MCP). External knowledge grounding: search grounding and verifiable citation binding. Vector databases and hybrid dense-sparse retrieval. Memory architectures: short-term buffers, episodic memory, and reflective memory. Sandboxed execution boundaries."
        },
        @{
            num = 4; hours = 16;
            title = "Unit 4: Evaluation, Safety & Production Deployment";
            content = "Agent evaluation benchmarks: AgentBench, GAIA, SWE-bench, and task completion metrics. Safety guardrails, prompt injection defenses, and jailbreak mitigation. Least-privilege authorization and tool permission scopes. Observability and audit logging: OpenTelemetry traces and execution replay. Token cost budgeting, model routing, caching, and streaming deployment."
        }
    )

    $t6 = $doc.Tables.Item(6)
    $t6.Cell(2, 1).Range.Text = "Unit - 1: Foundations of Agentic AI"
    $t6.Cell(3, 1).Range.Text = $units[0].content
    $t6.Cell(3, 2).Range.Text = "$($units[0].hours)"

    $t6.Cell(4, 1).Range.Text = "Unit - 2: Multi-Agent Swarms & Coordination"
    $t6.Cell(5, 1).Range.Text = $units[1].content
    $t6.Cell(5, 2).Range.Text = "$($units[1].hours)"

    $t6.Cell(6, 1).Range.Text = "Unit - 3: Tool-Augmented Reasoning & Knowledge Grounding"
    $t6.Cell(7, 1).Range.Text = $units[2].content
    $t6.Cell(7, 2).Range.Text = "$($units[2].hours)"

    # Add rows for Unit 4 cleanly
    $r8 = $t6.Rows.Add()
    $r8.Cells.Item(1).Range.Text = "Unit - 4: Evaluation, Safety & Production Deployment"
    $r8.Cells.Item(2).Range.Text = ""

    $r9 = $t6.Rows.Add()
    $r9.Cells.Item(1).Range.Text = $units[3].content
    $r9.Cells.Item(2).Range.Text = "$($units[3].hours)"

    Write-Host "Step 8: Populating Textbooks and References..."
    $tbRange = $doc.Content
    $tbCount = 0
    while ($tbRange.Find.Execute("[ENTER VERIFIED TEXTBOOK / REFERENCE]")) {
        if ($tbCount -eq 0) {
            $tbRange.Text = "1. Stuart Russell and Peter Norvig, 'Artificial Intelligence: A Modern Approach', 4th Edition, Pearson Education, 2020. ISBN: 978-0134610993."
        } elseif ($tbCount -eq 1) {
            $tbRange.Text = "2. David L. Poole and Alan K. Mackworth, 'Artificial Intelligence: Foundations of Computational Agents', 3rd Edition, Cambridge University Press, 2023. ISBN: 978-1009258401."
        } elseif ($tbCount -eq 2) {
            $tbRange.Text = "1. Shunyu Yao et al., 'ReAct: Synergizing Reasoning and Acting in Language Models', International Conference on Learning Representations (ICLR), 2023.`r`n2. Timo Schick et al., 'Toolformer: Language Models Can Teach Themselves to Use Tools', Advances in Neural Information Processing Systems (NeurIPS), 2023.`r`n3. ACM/IEEE-CS Computer Science Curricula 2023 (CS2023), Knowledge Area: Intelligent Systems (IS-Agent)."
        } else {
            $tbRange.Text = ""
        }
        $tbRange.Collapse(0)
        $tbCount++
    }

    Write-Host "Step 9: Populating Table 7 (Unitization for Exams)..."
    $t7 = $doc.Tables.Item(7)
    $t7.Cell(2, 1).Range.Text = "Unit 1: Foundations & Multi-Agent Swarms"
    $t7.Cell(3, 1).Range.Text = "Foundations of Agentic AI"
    $t7.Cell(3, 2).Range.Text = "10 Hours"
    $t7.Cell(3, 3).Range.Text = "2 Questions"
    $t7.Cell(3, 4).Range.Text = "-"
    $t7.Cell(3, 5).Range.Text = "1 Activity"
    $t7.Cell(3, 6).Range.Text = "2 Questions"

    $t7.Cell(4, 1).Range.Text = "Multi-Agent Coordination & Swarm Architectures"
    $t7.Cell(4, 2).Range.Text = "12 Hours"
    $t7.Cell(4, 3).Range.Text = "2 Questions"
    $t7.Cell(4, 4).Range.Text = "-"
    $t7.Cell(4, 5).Range.Text = "1 Activity"
    $t7.Cell(4, 6).Range.Text = "2 Questions"

    $t7.Cell(5, 1).Range.Text = "Unit 2: Tools, Grounding & Safety Deployment"
    $t7.Cell(6, 1).Range.Text = "Tool-Augmented Reasoning & Knowledge Grounding"
    $t7.Cell(6, 2).Range.Text = "14 Hours"
    $t7.Cell(6, 3).Range.Text = "-"
    $t7.Cell(6, 4).Range.Text = "2 Questions"
    $t7.Cell(6, 5).Range.Text = "1 Activity"
    $t7.Cell(6, 6).Range.Text = "2 Questions"

    $t7.Cell(7, 1).Range.Text = "Evaluation, Safety & Production Deployment"
    $t7.Cell(7, 2).Range.Text = "16 Hours"
    $t7.Cell(7, 3).Range.Text = "-"
    $t7.Cell(7, 4).Range.Text = "2 Questions"
    $t7.Cell(7, 5).Range.Text = "1 Activity"
    $t7.Cell(7, 6).Range.Text = "2 Questions"

    Write-Host "Step 10: Populating Table 8 (Department & HoD Sign-Off)..."
    $t8 = $doc.Tables.Item(8)
    $t8.Cell(1, 1).Range.Text = "Date: $(Get-Date -Format 'dd-MM-yyyy')"
    $t8.Cell(1, 2).Range.Text = "HoD, $Department"

    Write-Host "Step 11: Populating Table 9 (Course Assessment Overview - NO REINTRODUCED MARKS SCHEME)..."
    $t9 = $doc.Tables.Item(9)
    $t9.Cell(1, 1).Range.Text = "Course Title: $CourseTitle ($CourseCode)"
    # Do NOT inject fake ISA1/ISA2 numbers. Only list CO mapping to assessment methods
    $t9.Cell(4, 1).Range.Text = "CO1"
    $t9.Cell(4, 2).Range.Text = "-"
    $t9.Cell(4, 3).Range.Text = "Assessed"
    $t9.Cell(4, 4).Range.Text = "-"
    $t9.Cell(4, 5).Range.Text = "Assessed"
    $t9.Cell(4, 6).Range.Text = "-"
    $t9.Cell(4, 7).Range.Text = "Assessed"

    $t9.Cell(5, 1).Range.Text = "CO2"
    $t9.Cell(5, 2).Range.Text = "-"
    $t9.Cell(5, 3).Range.Text = "Assessed"
    $t9.Cell(5, 4).Range.Text = "-"
    $t9.Cell(5, 5).Range.Text = "Assessed"
    $t9.Cell(5, 6).Range.Text = "-"
    $t9.Cell(5, 7).Range.Text = "Assessed"

    $t9.Cell(6, 1).Range.Text = "CO3"
    $t9.Cell(6, 2).Range.Text = "-"
    $t9.Cell(6, 3).Range.Text = "-"
    $t9.Cell(6, 4).Range.Text = "Assessed"
    $t9.Cell(6, 5).Range.Text = "Assessed"
    $t9.Cell(6, 6).Range.Text = "-"
    $t9.Cell(6, 7).Range.Text = "Assessed"

    $t9.Cell(7, 1).Range.Text = "CO4"
    $t9.Cell(7, 2).Range.Text = "-"
    $t9.Cell(7, 3).Range.Text = "-"
    $t9.Cell(7, 4).Range.Text = "Assessed"
    $t9.Cell(7, 5).Range.Text = "Assessed"
    $t9.Cell(7, 6).Range.Text = "-"
    $t9.Cell(7, 7).Range.Text = "Assessed"

    $t9.Cell(8, 1).Range.Text = "-"
    for ($c = 2; $c -le 4; $c++) { $t9.Cell(8, $c).Range.Text = "-" }

    Write-Host "Step 12: Enforcing Conditional Lab Policy on Tables 10, 11, 12..."
    # Table 10: List of Experiments
    $t10 = $doc.Tables.Item(10)
    $t10.Cell(2, 1).Range.Text = "-"
    $t10.Cell(2, 2).Range.Text = "Not Applicable (Theory course with embedded practical demonstrations; no formal laboratory experiment sequence)."
    $t10.Cell(2, 3).Range.Text = "-"
    for ($r = $t10.Rows.Count; $r -ge 3; $r--) {
        $t10.Rows.Item($r).Delete()
    }

    # Table 11: Lab Evaluation Rubrics
    $t11 = $doc.Tables.Item(11)
    if ($t11.Range.Cells.Count -gt 14) {
        $startRange = $t11.Range.Cells.Item(15).Range
        $endRange = $t11.Range.Cells.Item($t11.Range.Cells.Count).Range
        $deleteRange = $doc.Range($startRange.Start, $endRange.End)
        $deleteRange.Select()
        $word.Selection.Rows.Delete()
    }
    $t11.Range.Cells.Item(8).Range.Text = "-"
    $t11.Range.Cells.Item(9).Range.Text = "Not Applicable (Theory course with embedded practical demonstrations; no formal laboratory experiment sequence)."
    for ($c = 10; $c -le 14; $c++) { $t11.Range.Cells.Item($c).Range.Text = "-" }

    # Table 12: Course Activity Evaluation Rubrics
    $t12 = $doc.Tables.Item(12)
    if ($t12.Range.Cells.Count -gt 14) {
        $startRange = $t12.Range.Cells.Item(15).Range
        $endRange = $t12.Range.Cells.Item($t12.Range.Cells.Count).Range
        $deleteRange = $doc.Range($startRange.Start, $endRange.End)
        $deleteRange.Select()
        $word.Selection.Rows.Delete()
    }
    $t12.Range.Cells.Item(8).Range.Text = "Course Activity: Autonomous Agent Pipeline Project"
    $t12.Range.Cells.Item(9).Range.Text = "Architecture, Tool Integration & Multi-Agent Coordination"
    $t12.Range.Cells.Item(10).Range.Text = "Working multi-agent system with tool calling, vector search grounding, and error recovery (4-5 M)"
    $t12.Range.Cells.Item(11).Range.Text = "Functional agent with basic tool usage and planning (2-3 M)"
    $t12.Range.Cells.Item(12).Range.Text = "Incomplete or non-functional execution (0-1 M)"
    $t12.Range.Cells.Item(13).Range.Text = "PI 3.1.2"
    $t12.Range.Cells.Item(14).Range.Text = "CO3"

    Write-Host "Step 13: Populating Chapter-Wise Lesson Plans (Tables 13 to 28 - UNIT SPECIFIC)..."

    # Chapter 1 Data (Tables 13, 14, 15, 16)
    $t13 = $doc.Tables.Item(13)
    $t13.Cell(1, 1).Range.Text = "Course Code and Title: $($CourseCode) - $($CourseTitle)"
    $t13.Cell(2, 1).Range.Text = "Chapter Number and Title: Unit 1: Foundations of Agentic AI & Autonomous Decision Systems"
    $t13.Cell(2, 2).Range.Text = "Planned Hours: 10 Hours"

    $t14 = $doc.Tables.Item(14)
    $t14.Cell(2, 1).Range.Text = "TLO 1.1: Formulate environment boundaries, perception spaces, and agency concepts"
    $t14.Cell(2, 2).Range.Text = "CO1"; $t14.Cell(2, 3).Range.Text = "L2"; $t14.Cell(2, 4).Range.Text = "CA1"
    $t14.Cell(3, 1).Range.Text = "TLO 1.2: Design goal decomposition trees and planning heuristics"
    $t14.Cell(3, 2).Range.Text = "CO1"; $t14.Cell(3, 3).Range.Text = "L3"; $t14.Cell(3, 4).Range.Text = "CA1"
    $t14.Cell(4, 1).Range.Text = "TLO 1.3: Implement iterative ReAct decision loops with trace logging"
    $t14.Cell(4, 2).Range.Text = "CO1"; $t14.Cell(4, 3).Range.Text = "L3"; $t14.Cell(4, 4).Range.Text = "CA1"
    $t14.Cell(5, 1).Range.Text = "TLO 1.4: Analyze error reflection and self-correction heuristics"
    $t14.Cell(5, 2).Range.Text = "CO1"; $t14.Cell(5, 3).Range.Text = "L3"; $t14.Cell(5, 4).Range.Text = "CA1"

    $t15 = $doc.Tables.Item(15)
    $sched1 = @(
        "Class 1: Introduction to Agentic AI: Autonomous agents vs classical LLM prompting",
        "Class 2: Cognitive Architectures: Environment perception, memory, and action spaces",
        "Class 3: Decision-Making Loops: Observe-Orient-Decide-Act (OODA) and ReAct paradigms",
        "Class 4: Goal formulation, task decomposition, and hierarchical planning trees",
        "Class 5: State tracking, execution traces, and error reflection mechanisms",
        "Class 6: Plan-and-solve vs interleaved reason-action execution strategies",
        "Class 7: Hands-on demonstration: Constructing a single-agent task loop in Python"
    )
    for ($i = 0; $i -lt $sched1.Count; $i++) {
        $t15.Cell($i + 2, 1).Range.Text = $sched1[$i]
    }

    $t16 = $doc.Tables.Item(16)
    $t16.Cell(2, 1).Range.Text = "1"
    $t16.Cell(2, 2).Range.Text = "Explain how the ReAct framework mitigates hallucination compared to single-turn prompt generation."
    $t16.Cell(2, 3).Range.Text = "TLO 1.1"; $t16.Cell(2, 4).Range.Text = "L2"; $t16.Cell(2, 5).Range.Text = "PI 1.3.1"
    $t16.Cell(3, 1).Range.Text = "2"
    $t16.Cell(3, 2).Range.Text = "Formulate a task decomposition tree for an agent required to synthesize and compare financial reports."
    $t16.Cell(3, 3).Range.Text = "TLO 1.2"; $t16.Cell(3, 4).Range.Text = "L3"; $t16.Cell(3, 5).Range.Text = "PI 2.2.4"
    $t16.Cell(4, 1).Range.Text = "3"
    $t16.Cell(4, 2).Range.Text = "Differentiate between iterative replanning and static prompt chaining in terms of error recovery."
    $t16.Cell(4, 3).Range.Text = "TLO 1.3"; $t16.Cell(4, 4).Range.Text = "L3"; $t16.Cell(4, 5).Range.Text = "PI 3.1.2"
    for ($r = 5; $r -le $t16.Rows.Count; $r++) {
        $t16.Cell($r, 1).Range.Text = "-"
        $t16.Cell($r, 2).Range.Text = "[Pending Faculty Question Bank Contribution]"
        $t16.Cell($r, 3).Range.Text = "-"
        $t16.Cell($r, 4).Range.Text = "-"
        $t16.Cell($r, 5).Range.Text = "-"
    }

    # Chapter 2 Data (Tables 17, 18, 19, 20)
    $t17 = $doc.Tables.Item(17)
    $t17.Cell(1, 1).Range.Text = "Course Code and Title: $($CourseCode) - $($CourseTitle)"
    $t17.Cell(2, 1).Range.Text = "Chapter Number and Title: Unit 2: Multi-Agent Coordination & Swarm Architectures"
    $t17.Cell(2, 2).Range.Text = "Planned Hours: 12 Hours"

    $t18 = $doc.Tables.Item(18)
    $t18.Cell(2, 1).Range.Text = "TLO 2.1: Model inter-agent communication protocols and message schemas"
    $t18.Cell(2, 2).Range.Text = "CO2"; $t18.Cell(2, 3).Range.Text = "L3"; $t18.Cell(2, 4).Range.Text = "CA2"
    $t18.Cell(3, 1).Range.Text = "TLO 2.2: Design supervisor-worker and decentralized peer swarm topologies"
    $t18.Cell(3, 2).Range.Text = "CO2"; $t18.Cell(3, 3).Range.Text = "L3"; $t18.Cell(3, 4).Range.Text = "CA2"
    $t18.Cell(4, 1).Range.Text = "TLO 2.3: Implement consensus protocols and voting mechanisms"
    $t18.Cell(4, 2).Range.Text = "CO2"; $t18.Cell(4, 3).Range.Text = "L4"; $t18.Cell(4, 4).Range.Text = "CA2"
    for ($r = 5; $r -le $t18.Rows.Count; $r++) {
        $t18.Cell($r, 1).Range.Text = "-"
        $t18.Cell($r, 2).Range.Text = "-"
        $t18.Cell($r, 3).Range.Text = "-"
        $t18.Cell($r, 4).Range.Text = "-"
    }

    $t19 = $doc.Tables.Item(19)
    $sched2 = @(
        "Class 1: Multi-Agent Systems: Coordination paradigms and agent taxonomies",
        "Class 2: Communication protocols: Blackboard architectures vs direct RPC messaging",
        "Class 3: Collaborative swarms vs competitive multi-agent game-theoretic dynamics",
        "Class 4: Supervisor patterns: Task delegation, subagent spawning, and status monitoring",
        "Class 5: Consensus protocols and voting mechanisms for ensemble validation",
        "Class 6: Failure mode mitigation: Deadlocks, circular dependencies, and runaway loops",
        "Class 7: Hands-on implementation: Multi-agent coordination with shared workspace",
        "Class 8: Review & case study: SWE-agent multi-agent software engineering team"
    )
    for ($i = 0; $i -lt $sched2.Count; $i++) {
        $t19.Cell($i + 2, 1).Range.Text = $sched2[$i]
    }

    $t20 = $doc.Tables.Item(20)
    $t20.Cell(2, 1).Range.Text = "1"
    $t20.Cell(2, 2).Range.Text = "Compare blackboard-based communication with direct peer-to-peer message passing in multi-agent swarms."
    $t20.Cell(2, 3).Range.Text = "TLO 2.1"; $t20.Cell(2, 4).Range.Text = "L3"; $t20.Cell(2, 5).Range.Text = "PI 2.2.4"
    $t20.Cell(3, 1).Range.Text = "2"
    $t20.Cell(3, 2).Range.Text = "Design a supervisor-worker topology to conduct parallel code refactoring and test verification."
    $t20.Cell(3, 3).Range.Text = "TLO 2.2"; $t20.Cell(3, 4).Range.Text = "L3"; $t20.Cell(3, 5).Range.Text = "PI 3.1.2"
    $t20.Cell(4, 1).Range.Text = "3"
    $t20.Cell(4, 2).Range.Text = "Analyze the vulnerability of multi-agent consensus algorithms to Byzantine failure or agent drift."
    $t20.Cell(4, 3).Range.Text = "TLO 2.3"; $t20.Cell(4, 4).Range.Text = "L4"; $t20.Cell(4, 5).Range.Text = "PI 5.2.1"
    for ($r = 5; $r -le $t20.Rows.Count; $r++) {
        $t20.Cell($r, 1).Range.Text = "-"
        $t20.Cell($r, 2).Range.Text = "[Pending Faculty Question Bank Contribution]"
        $t20.Cell($r, 3).Range.Text = "-"
        $t20.Cell($r, 4).Range.Text = "-"
        $t20.Cell($r, 5).Range.Text = "-"
    }

    # Chapter 3 Data (Tables 21, 22, 23, 24)
    $t21 = $doc.Tables.Item(21)
    $t21.Cell(1, 1).Range.Text = "Course Code and Title: $($CourseCode) - $($CourseTitle)"
    $t21.Cell(2, 1).Range.Text = "Chapter Number and Title: Unit 3: Tool-Augmented Reasoning & External Grounding"
    $t21.Cell(2, 2).Range.Text = "Planned Hours: 14 Hours"

    $t22 = $doc.Tables.Item(22)
    $t22.Cell(2, 1).Range.Text = "TLO 3.1: Formulate function calling schemas, tool registration, and parameter serialization"
    $t22.Cell(2, 2).Range.Text = "CO2"; $t22.Cell(2, 3).Range.Text = "L3"; $t22.Cell(2, 4).Range.Text = "CA3"
    $t22.Cell(3, 1).Range.Text = "TLO 3.2: Integrate vector database retrieval and hybrid search for external knowledge grounding"
    $t22.Cell(3, 2).Range.Text = "CO3"; $t22.Cell(3, 3).Range.Text = "L3"; $t22.Cell(3, 4).Range.Text = "CA3"
    $t22.Cell(4, 1).Range.Text = "TLO 3.3: Design persistent memory buffers and state snapshotting mechanisms"
    $t22.Cell(4, 2).Range.Text = "CO3"; $t22.Cell(4, 3).Range.Text = "L4"; $t22.Cell(4, 4).Range.Text = "CA3"
    for ($r = 5; $r -le $t22.Rows.Count; $r++) {
        $t22.Cell($r, 1).Range.Text = "-"
        $t22.Cell($r, 2).Range.Text = "-"
        $t22.Cell($r, 3).Range.Text = "-"
        $t22.Cell($r, 4).Range.Text = "-"
    }

    $t23 = $doc.Tables.Item(23)
    $sched3 = @(
        "Class 1: Tool-augmented reasoning: Extending LLMs with external computation engines",
        "Class 2: Schema definition and governed tool registries: Model Context Protocol (MCP)",
        "Class 3: Search grounding: Integrating live web queries, citation binding, and verification",
        "Class 4: Vector stores for long-term memory: Chunking strategies and hybrid retrieval",
        "Class 5: Memory architectures: Short-term buffers, episodic memory, and reflective memory",
        "Class 6: Sandboxed execution environments and tool permission boundaries",
        "Class 7: Hands-on implementation: Tool-calling agent with vector search grounding"
    )
    for ($i = 0; $i -lt $sched3.Count; $i++) {
        $t23.Cell($i + 2, 1).Range.Text = $sched3[$i]
    }

    $t24 = $doc.Tables.Item(24)
    $t24.Cell(2, 1).Range.Text = "1"
    $t24.Cell(2, 2).Range.Text = "Explain how JSON schema validation prevents malformed tool parameter injection during agent execution."
    $t24.Cell(2, 3).Range.Text = "TLO 3.1"; $t24.Cell(2, 4).Range.Text = "L3"; $t24.Cell(2, 5).Range.Text = "PI 3.1.2"
    $t24.Cell(3, 1).Range.Text = "2"
    $t24.Cell(3, 2).Range.Text = "Formulate a hybrid dense-sparse retrieval pipeline to provide verifiable evidence for agent claims."
    $t24.Cell(3, 3).Range.Text = "TLO 3.2"; $t24.Cell(3, 4).Range.Text = "L3"; $t24.Cell(3, 5).Range.Text = "PI 5.2.1"
    $t24.Cell(4, 1).Range.Text = "3"
    $t24.Cell(4, 2).Range.Text = "Discuss memory compaction techniques to preserve context window limits in long-running agent workflows."
    $t24.Cell(4, 3).Range.Text = "TLO 3.3"; $t24.Cell(4, 4).Range.Text = "L4"; $t24.Cell(4, 5).Range.Text = "PI 2.2.4"
    for ($r = 5; $r -le $t24.Rows.Count; $r++) {
        $t24.Cell($r, 1).Range.Text = "-"
        $t24.Cell($r, 2).Range.Text = "[Pending Faculty Question Bank Contribution]"
        $t24.Cell($r, 3).Range.Text = "-"
        $t24.Cell($r, 4).Range.Text = "-"
        $t24.Cell($r, 5).Range.Text = "-"
    }

    # Chapter 4 Data (Tables 25, 26, 27, 28)
    $t25 = $doc.Tables.Item(25)
    $t25.Cell(1, 1).Range.Text = "Course Code and Title: $($CourseCode) - $($CourseTitle)"
    $t25.Cell(2, 1).Range.Text = "Chapter Number and Title: Unit 4: Evaluation, Safety & Production Deployment"
    $t25.Cell(2, 2).Range.Text = "Planned Hours: 16 Hours"

    $t26 = $doc.Tables.Item(26)
    $t26.Cell(2, 1).Range.Text = "TLO 4.1: Evaluate autonomous agents against standardized benchmarks (AgentBench, GAIA)"
    $t26.Cell(2, 2).Range.Text = "CO3"; $t26.Cell(2, 3).Range.Text = "L4"; $t26.Cell(2, 4).Range.Text = "CA4"
    $t26.Cell(3, 1).Range.Text = "TLO 4.2: Implement security guardrails against prompt injection and unauthorized privilege escalation"
    $t26.Cell(3, 2).Range.Text = "CO4"; $t26.Cell(3, 3).Range.Text = "L4"; $t26.Cell(3, 4).Range.Text = "CA4"
    $t26.Cell(4, 1).Range.Text = "TLO 4.3: Deploy low-latency agent inference pipelines with token cost budgeting and observability"
    $t26.Cell(4, 2).Range.Text = "CO4"; $t26.Cell(4, 3).Range.Text = "L4"; $t26.Cell(4, 4).Range.Text = "CA4"
    for ($r = 5; $r -le $t26.Rows.Count; $r++) {
        $t26.Cell($r, 1).Range.Text = "-"
        $t26.Cell($r, 2).Range.Text = "-"
        $t26.Cell($r, 3).Range.Text = "-"
        $t26.Cell($r, 4).Range.Text = "-"
    }

    $t27 = $doc.Tables.Item(27)
    $sched4 = @(
        "Class 1: Agent evaluation paradigms: Task completion rate, step efficiency, and drift",
        "Class 2: Benchmark suites: SWE-bench, GAIA, WebArena, and human evaluation protocols",
        "Class 3: Safety and alignment: Guardrails, prompt injection defenses, and jailbreak mitigation",
        "Class 4: Governance and permission boundaries: Least-privilege agent authorization",
        "Class 5: Observability and audit logging: OpenTelemetry traces and execution replay",
        "Class 6: Token budgeting, cost optimization, model routing, and caching strategies",
        "Class 7: Production deployment: Containerization, streaming responses, and reliability engineering",
        "Class 8: Capstone review: Ethical boundaries and autonomous agent governance"
    )
    for ($i = 0; $i -lt $sched4.Count; $i++) {
        $t27.Cell($i + 2, 1).Range.Text = $sched4[$i]
    }

    $t28 = $doc.Tables.Item(28)
    $t28.Cell(2, 1).Range.Text = "1"
    $t28.Cell(2, 2).Range.Text = "Describe three quantitative metrics used to evaluate autonomous agent planning performance."
    $t28.Cell(2, 3).Range.Text = "TLO 4.1"; $t28.Cell(2, 4).Range.Text = "L4"; $t28.Cell(2, 5).Range.Text = "PI 3.1.2"
    $t28.Cell(3, 1).Range.Text = "2"
    $t28.Cell(3, 2).Range.Text = "Analyze security risks when an autonomous agent is granted terminal command execution privileges."
    $t28.Cell(3, 3).Range.Text = "TLO 4.2"; $t28.Cell(3, 4).Range.Text = "L4"; $t28.Cell(3, 5).Range.Text = "PI 5.2.1"
    $t28.Cell(4, 1).Range.Text = "3"
    $t28.Cell(4, 2).Range.Text = "Design an observability architecture to capture step-by-step audit logs of multi-agent decisions."
    $t28.Cell(4, 3).Range.Text = "TLO 4.3"; $t28.Cell(4, 4).Range.Text = "L4"; $t28.Cell(4, 5).Range.Text = "PI 2.2.4"
    for ($r = 5; $r -le $t28.Rows.Count; $r++) {
        $t28.Cell($r, 1).Range.Text = "-"
        $t28.Cell($r, 2).Range.Text = "-"
        $t28.Cell($r, 3).Range.Text = "-"
        $t28.Cell($r, 4).Range.Text = "-"
        $t28.Cell($r, 5).Range.Text = "-"
    }

    Write-Host "Step 14: Populating Model Question Papers (Tables 29, 30, 31 - AUTHENTIC ASSESSMENT QUESTIONS)..."
    # Table 29: Minor Exam 1
    $t29 = $doc.Tables.Item(29)
    $t29.Cell(1, 1).Range.Text = "Model Question Paper: Minor Exam - 1"
    $t29.Cell(2, 1).Range.Text = "Course Code: $CourseCode"
    $t29.Cell(2, 2).Range.Text = "Course Title: $CourseTitle"
    $t29.Cell(3, 1).Range.Text = "Duration: 1 Hour"
    $t29.Cell(3, 2).Range.Text = "Maximum Marks: 20"
    $t29.Cell(4, 1).Range.Text = "Instructions: Answer any TWO full questions. All questions carry equal marks (10 Marks each)."

    $t29.Cell(5, 1).Range.Text = "Q1 (a)"
    $t29.Cell(5, 2).Range.Text = "Explain the foundational architecture of an autonomous agent. Detail the interaction between perception, memory, planning, and execution."
    $t29.Cell(5, 3).Range.Text = "CO1"; $t29.Cell(5, 4).Range.Text = "L2"; $t29.Cell(5, 5).Range.Text = "5"; $t29.Cell(5, 6).Range.Text = "PI 1.3.1"; $t29.Cell(5, 7).Range.Text = "-"

    $t29.Cell(6, 1).Range.Text = "Q1 (b)"
    $t29.Cell(6, 2).Range.Text = "Formulate a ReAct decision loop for an agent querying an external SQL database to retrieve student enrollment records."
    $t29.Cell(6, 3).Range.Text = "CO1"; $t29.Cell(6, 4).Range.Text = "L3"; $t29.Cell(6, 5).Range.Text = "5"; $t29.Cell(6, 6).Range.Text = "PI 2.2.4"; $t29.Cell(6, 7).Range.Text = "-"

    $t29.Cell(7, 1).Range.Text = "Q2 (a)"
    $t29.Cell(7, 2).Range.Text = "Differentiate between tree-search planning (e.g., Tree of Thoughts) and sequential linear reasoning in agentic problem solving."
    $t29.Cell(7, 3).Range.Text = "CO1"; $t29.Cell(7, 4).Range.Text = "L2"; $t29.Cell(7, 5).Range.Text = "5"; $t29.Cell(7, 6).Range.Text = "PI 1.3.1"; $t29.Cell(7, 7).Range.Text = "-"

    $t29.Cell(8, 1).Range.Text = "Q2 (b)"
    $t29.Cell(8, 2).Range.Text = "Illustrate how error reflection mechanisms enable autonomous agents to self-correct upon receiving API tool exceptions."
    $t29.Cell(8, 3).Range.Text = "CO1"; $t29.Cell(8, 4).Range.Text = "L3"; $t29.Cell(8, 5).Range.Text = "5"; $t29.Cell(8, 6).Range.Text = "PI 3.1.2"; $t29.Cell(8, 7).Range.Text = "-"

    for ($r = 9; $r -le $t29.Rows.Count; $r++) {
        for ($c = 1; $c -le 7; $c++) { $t29.Cell($r, $c).Range.Text = "-" }
    }

    # Table 30: Minor Exam 2
    $t30 = $doc.Tables.Item(30)
    $t30.Cell(1, 1).Range.Text = "Model Question Paper: Minor Exam - 2"
    $t30.Cell(2, 1).Range.Text = "Course Code: $CourseCode"
    $t30.Cell(2, 2).Range.Text = "Course Title: $CourseTitle"
    $t30.Cell(3, 1).Range.Text = "Duration: 1 Hour"
    $t30.Cell(3, 2).Range.Text = "Maximum Marks: 20"
    $t30.Cell(4, 1).Range.Text = "Instructions: Answer any TWO full questions. All questions carry equal marks (10 Marks each)."

    $t30.Cell(5, 1).Range.Text = "Q1 (a)"
    $t30.Cell(5, 2).Range.Text = "Contrast centralized supervisor-worker multi-agent coordination with peer-to-peer blackboard communication."
    $t30.Cell(5, 3).Range.Text = "CO2"; $t30.Cell(5, 4).Range.Text = "L3"; $t30.Cell(5, 5).Range.Text = "5"; $t30.Cell(5, 6).Range.Text = "PI 2.2.4"; $t30.Cell(5, 7).Range.Text = "-"

    $t30.Cell(6, 1).Range.Text = "Q1 (b)"
    $t30.Cell(6, 2).Range.Text = "Design a function-calling JSON schema for a weather retrieval tool with latitude, longitude, and forecast units."
    $t30.Cell(6, 3).Range.Text = "CO2"; $t30.Cell(6, 4).Range.Text = "L3"; $t30.Cell(6, 5).Range.Text = "5"; $t30.Cell(6, 6).Range.Text = "PI 3.1.2"; $t30.Cell(6, 7).Range.Text = "-"

    $t30.Cell(7, 1).Range.Text = "Q2 (a)"
    $t30.Cell(7, 2).Range.Text = "Explain how hybrid dense-sparse vector search grounding prevents hallucination in retrieval-augmented agents."
    $t30.Cell(7, 3).Range.Text = "CO3"; $t30.Cell(7, 4).Range.Text = "L3"; $t30.Cell(7, 5).Range.Text = "5"; $t30.Cell(7, 6).Range.Text = "PI 5.2.1"; $t30.Cell(7, 7).Range.Text = "-"

    $t30.Cell(8, 1).Range.Text = "Q2 (b)"
    $t30.Cell(8, 2).Range.Text = "Analyze deadlock prevention strategies in asynchronous multi-agent message-passing networks."
    $t30.Cell(8, 3).Range.Text = "CO2"; $t30.Cell(8, 4).Range.Text = "L4"; $t30.Cell(8, 5).Range.Text = "5"; $t30.Cell(8, 6).Range.Text = "PI 3.1.2"; $t30.Cell(8, 7).Range.Text = "-"

    for ($r = 9; $r -le $t30.Rows.Count; $r++) {
        for ($c = 1; $c -le 7; $c++) { $t30.Cell($r, $c).Range.Text = "-" }
    }

    # Table 31: Semester End Examination (ESA)
    $t31 = $doc.Tables.Item(31)
    $t31.Cell(1, 1).Range.Text = "Model Question Paper: Semester End Examination (ESA)"
    $t31.Cell(2, 1).Range.Text = "Course Code: $CourseCode"
    $t31.Cell(2, 2).Range.Text = "Course Title: $CourseTitle"
    $t31.Cell(3, 1).Range.Text = "Duration: 3 Hours"
    $t31.Cell(3, 2).Range.Text = "Maximum Marks: 100"
    $t31.Cell(4, 1).Range.Text = "Instructions: Answer any FIVE full questions choosing ONE from each unit. All questions carry equal marks (20 Marks each)."

    $t31.Cell(5, 1).Range.Text = "Part A: Questions (Answer any FIVE full questions choosing ONE from each unit)"

    $t31.Cell(6, 1).Range.Text = "Q1 (a)"
    $t31.Cell(6, 2).Range.Text = "Formulate the mathematical state-transition model of an autonomous agent operating under partial observability."
    $t31.Cell(6, 3).Range.Text = "CO1"; $t31.Cell(6, 4).Range.Text = "L3"; $t31.Cell(6, 5).Range.Text = "10"; $t31.Cell(6, 6).Range.Text = "PI 1.3.1"; $t31.Cell(6, 7).Range.Text = "-"

    $t31.Cell(7, 1).Range.Text = "Q1 (b)"
    $t31.Cell(7, 2).Range.Text = "Explain task decomposition heuristics and demonstrate how hierarchical planning reduces sub-goal search complexity."
    $t31.Cell(7, 3).Range.Text = "CO1"; $t31.Cell(7, 4).Range.Text = "L3"; $t31.Cell(7, 5).Range.Text = "10"; $t31.Cell(7, 6).Range.Text = "PI 2.2.4"; $t31.Cell(7, 7).Range.Text = "-"

    $t31.Cell(8, 1).Range.Text = "Q2 (a)"
    $t31.Cell(8, 2).Range.Text = "Design a multi-agent swarm architecture for collaborative code synthesis, unit testing, and security auditing."
    $t31.Cell(8, 3).Range.Text = "CO2"; $t31.Cell(8, 4).Range.Text = "L4"; $t31.Cell(8, 5).Range.Text = "10"; $t31.Cell(8, 6).Range.Text = "PI 3.1.2"; $t31.Cell(8, 7).Range.Text = "-"

    $t31.Cell(9, 1).Range.Text = "Q2 (b)"
    $t31.Cell(9, 2).Range.Text = "Evaluate consensus protocols in multi-agent verification ensembles when agent responses diverge."
    $t31.Cell(9, 3).Range.Text = "CO2"; $t31.Cell(9, 4).Range.Text = "L4"; $t31.Cell(9, 5).Range.Text = "10"; $t31.Cell(9, 6).Range.Text = "PI 5.2.1"; $t31.Cell(9, 7).Range.Text = "-"

    $t31.Cell(10, 1).Range.Text = "Q3 (a)"
    $t31.Cell(10, 2).Range.Text = "Analyze tool-augmented reasoning via Model Context Protocol (MCP). How are permission scopes and safety boundaries enforced?"
    $t31.Cell(10, 3).Range.Text = "CO3"; $t31.Cell(10, 4).Range.Text = "L3"; $t31.Cell(10, 5).Range.Text = "10"; $t31.Cell(10, 6).Range.Text = "PI 3.1.2"; $t31.Cell(10, 7).Range.Text = "-"

    $t31.Cell(11, 1).Range.Text = "Q3 (b)"
    $t31.Cell(11, 2).Range.Text = "Formulate a memory management strategy combining short-term buffers, semantic vector indexing, and episodic reflection."
    $t31.Cell(11, 3).Range.Text = "CO3"; $t31.Cell(11, 4).Range.Text = "L4"; $t31.Cell(11, 5).Range.Text = "10"; $t31.Cell(11, 6).Range.Text = "PI 5.2.1"; $t31.Cell(11, 7).Range.Text = "-"

    $t31.Cell(12, 1).Range.Text = "Q4 (a)"
    $t31.Cell(12, 2).Range.Text = "Discuss prompt injection defenses and privilege escalation vulnerabilities in agentic systems with shell access."
    $t31.Cell(12, 3).Range.Text = "CO4"; $t31.Cell(12, 4).Range.Text = "L4"; $t31.Cell(12, 5).Range.Text = "10"; $t31.Cell(12, 6).Range.Text = "PI 5.2.1"; $t31.Cell(12, 7).Range.Text = "-"

    $t31.Cell(13, 1).Range.Text = "-"

    $t31.Cell(14, 1).Range.Text = "Q4 (b)"
    $t31.Cell(14, 2).Range.Text = "Design an end-to-end evaluation benchmark to measure task completion rate, tool call accuracy, and token latency."
    $t31.Cell(14, 3).Range.Text = "CO4"; $t31.Cell(14, 4).Range.Text = "L4"; $t31.Cell(14, 5).Range.Text = "10"; $t31.Cell(14, 6).Range.Text = "PI 3.1.2"; $t31.Cell(14, 7).Range.Text = "-"

    for ($r = 15; $r -le $t31.Rows.Count; $r++) {
        $cCount = $t31.Rows.Item($r).Cells.Count
        for ($c = 1; $c -le $cCount; $c++) { $t31.Cell($r, $c).Range.Text = "-" }
    }

    Write-Host "Step 15: Scanning and Clearing Any Lingering Bracketed Placeholders..."
    $phRange = $doc.Content
    $phMatches = [regex]::Matches($phRange.Text, '\[[A-Z0-9_\s/:,\-\.]+\]')
    $unresolved = $phMatches | ForEach-Object { $_.Value } | Select-Object -Unique
    Write-Host "Remaining placeholders count: $($unresolved.Count)"
    foreach ($ph in $unresolved) {
        Write-Host "Resolving lingering placeholder: $ph"
        Doc-Replace $ph "-"
    }

    $doc.Save()
    Write-Host "DOCX Saved successfully: $DocxOutputPath"

    Write-Host "Step 16: Exporting Native Pixel-Perfect PDF via Microsoft Word..."
    $wdFormatPDF = 17
    $doc.SaveAs([ref]$PdfOutputPath, [ref]$wdFormatPDF)
    Write-Host "PDF Exported successfully: $PdfOutputPath"

} finally {
    if ($doc -ne $null) {
        $doc.Close([ref]$false)
    }
    $word.Quit()
}

Write-Host "`nAll steps completed successfully!"
