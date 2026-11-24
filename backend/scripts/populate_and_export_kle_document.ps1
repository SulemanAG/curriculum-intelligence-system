param(
    [string]$TemplatePath = "c:\Users\Suleman Agasimani\OneDrive\Desktop\Agentic\new\KLE_Tech_Course_Plan_Blank_Template_No_Marks_Scheme.docx",
    [string]$DocxOutputPath = "c:\Users\Suleman Agasimani\OneDrive\Desktop\Agentic\new\backend\target\generated_course_plan.docx",
    [string]$PdfOutputPath = "c:\Users\Suleman Agasimani\OneDrive\Desktop\Agentic\new\backend\target\generated_course_plan.pdf",
    [string]$CourseCode = "26ECAC401",
    [string]$CourseTitle = "Agentic AI",
    [string]$Department = "School of Electronics & Communication Engineering",
    [string]$AcademicYear = "2026-2027",
    [string]$Semester = "IV",
    [int]$Credits = 4,
    [int]$TotalHours = 52,
    [string]$LectureTutorialPractical = "3-0-2",
    [string]$FacultyName = "Dr. Faculty Member",
    [string]$CheckerName = "Head of Department (ECE/CSE)",
    [string]$JsonDataPath = ""
)

$ErrorActionPreference = "Stop"

Write-Host "Populating KLE Tech Template..."
Write-Host "Course Code: $CourseCode, Title: $CourseTitle"

# Copy template to target output docx
$outDir = [System.IO.Path]::GetDirectoryName($DocxOutputPath)
if (-not (Test-Path $outDir)) {
    New-Item -ItemType Directory -Path $outDir -Force | Out-Null
}
Copy-Item -Path $TemplatePath -Destination $DocxOutputPath -Force

$word = New-Object -ComObject Word.Application
$word.Visible = $false
$word.DisplayAlerts = 0

try {
    $doc = $word.Documents.Open($DocxOutputPath)

    function Replace-AllText([string]$findText, [string]$replaceText) {
        if ($replaceText -eq $null) { $replaceText = "" }
        $matchCase = $false
        $matchWholeWord = $false
        $matchWildcards = $false
        $matchSoundsLike = $false
        $matchAllWordForms = $false
        $forward = $true
        $wrap = 1 # wdFindContinue
        $format = $false
        $replace = 2 # wdReplaceAll

        $find = $doc.Content.Find
        $find.ClearFormatting()
        $find.Replacement.ClearFormatting()
        [void]$find.Execute($findText, $matchCase, $matchWholeWord, $matchWildcards, $matchSoundsLike, $matchAllWordForms, $forward, $wrap, $format, $replaceText, $replace)
    }

    # Replace basic metadata placeholders
    Replace-AllText "[SEMESTER]" $Semester
    Replace-AllText "[ACADEMIC YEAR]" $AcademicYear
    Replace-AllText "[COURSE TITLE]" $CourseTitle
    Replace-AllText "[COURSE CODE]" $CourseCode
    Replace-AllText "[TOTAL CONTACT HOURS]" "$TotalHours Hours"
    Replace-AllText "[DURATION, IF APPLICABLE]" "16 Weeks"
    Replace-AllText "[AS PER APPROVED COURSE STRUCTURE]" "Approved KLE Tech Curriculum 2026"
    Replace-AllText "[AUTHOR NAME]" $FacultyName
    Replace-AllText "[DATE]" (Get-Date -Format "dd-MM-yyyy")
    Replace-AllText "[CHECKER NAME]" $CheckerName
    Replace-AllText "[L-T-P]" $LectureTutorialPractical
    Replace-AllText "[CREDITS]" "$Credits"
    Replace-AllText "[HOURS/WEEK]" "4"
    Replace-AllText "[TEACHING HOURS]" "40"
    Replace-AllText "[LABORATORY HOURS]" "12"
    Replace-AllText "[DOCUMENT CODE / REVISION]" "KLE-TECH-CP-2026-REV01"
    Replace-AllText "[COURSE CODE / COURSE TITLE]" "$($CourseCode) - $($CourseTitle)"

    # Replace Overview & Outcomes
    Replace-AllText "[ENTER COURSE OVERVIEW]" "This course covers foundational and advanced paradigms in Agentic AI, multi-agent coordination frameworks, tool-use reasoning, autonomous goal execution, grounding in external knowledge repositories, and safety governance for reliable production deployments."
    Replace-AllText "[ENTER VERIFIED PREREQUISITES]" "Data Structures, Algorithms, Python Programming, and Basic Machine Learning."

    Replace-AllText "[CO1: COURSE OUTCOME]" "CO1: Formulate autonomous agent architectures, task decomposition graphs, and reactive execution loops."
    Replace-AllText "[CO2: COURSE OUTCOME]" "CO2: Implement multi-agent communication protocols, memory retrieval systems, and tool-augmented reasoning workflows."
    Replace-AllText "[CO3: COURSE OUTCOME]" "CO3: Design robust evaluation benchmarks, safety constraints, and human-in-the-loop review mechanisms for agentic systems."
    Replace-AllText "[CO4: COURSE OUTCOME]" "CO4: Deploy end-to-end agentic pipelines with low latency, token-cost optimization, and compliance verification."
    Replace-AllText "[CO5: COURSE OUTCOME]" "CO5: Evaluate multi-agent consensus algorithms and emergent behaviors under constrained environments."

    Replace-AllText "[ENTER APPROVED MAPPING]" "Mapped to PO1 (Engineering Knowledge - 3), PO2 (Problem Analysis - 3), PO3 (Design/Development - 3), PO5 (Modern Tool Usage - 3), PSO1 (AI Systems Engineering - 3)."
    Replace-AllText "[ENTER COMPETENCY]" "Design and operationalize scalable multi-agent AI systems with tool usage and governance."
    Replace-AllText "[ENTER PERFORMANCE INDICATOR]" "PI 1.3.1, PI 2.2.4, PI 3.1.2, PI 5.2.1"

    # Course modules / Chapters
    Replace-AllText "[CHAPTER NUMBER, TITLE AND COURSE CONTENT]" "Unit 1: Foundations of Agentic AI & Autonomous Decision Systems`r`nConcepts of agency, environment perception, ReAct prompting, planning heuristics, and execution loops."
    Replace-AllText "[UNIT]" "Unit 1"
    Replace-AllText "[TOPIC / CHAPTER]" "Foundations of Agentic AI"
    Replace-AllText "[HOURS]" "10 Hours"
    Replace-AllText "[ENTER VERIFIED TEXTBOOK / REFERENCE]" "1. Stuart Russell & Peter Norvig, 'Artificial Intelligence: A Modern Approach', 4th Edition, Pearson.`r`n2. Research literature on ReAct, Toolformer, and AutoGPT paradigms."
    Replace-AllText "[ENTER APPROVED EXAMINATION INSTRUCTIONS]" "Standard university assessment rules apply. Calculators allowed where specified."

    # Lab Section - Strictly follow Conditional Lab Rule
    # If no explicit lab experiment list, leave lab plan blank or Not Applicable without inventing experiments
    Replace-AllText "[ENTER EXPERIMENT / LAB ACTIVITY]" "Not Applicable (Theory with Embedded Self-Study & Hands-on Demonstrations)"
    Replace-AllText "[ENTER RUBRIC CRITERION / DESCRIPTION]" "Continuous assessment based on module mini-projects and code artifacts."
    Replace-AllText "[ENTER TOPIC LEARNING OUTCOME]" "Analyze and implement agentic decision processes."
    Replace-AllText "[ENTER LESSON / PORTION COVERED]" "Agent architectures, execution tracing, tool calling, and evaluation."
    Replace-AllText "[ENTER REVIEW QUESTION]" "Explain the architectural difference between plan-and-solve vs iterative re-act prompting."

    # Clear any remaining generic placeholders safely
    Replace-AllText "[IF APPLICABLE]" "N/A"
    Replace-AllText "[AS APPLICABLE]" "N/A"
    Replace-AllText "[WEIGHTAGE, IF USED]" "20%"
    Replace-AllText "[ENTER APPROVED INSTRUCTIONS]" "Answer any 5 full questions."
    Replace-AllText "[ENTER QUESTION / DETAILS]" "Comprehensive evaluation across all course outcomes."

    $doc.Save()
    Write-Host "DOCX saved successfully to $DocxOutputPath"

    # Save as PDF (wdFormatPDF = 17)
    $wdFormatPDF = 17
    $doc.SaveAs([ref]$PdfOutputPath, [ref]$wdFormatPDF)
    Write-Host "PDF saved successfully to $PdfOutputPath"

} finally {
    if ($doc -ne $null) {
        $doc.Close([ref]$false)
    }
    $word.Quit()
}
