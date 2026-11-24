import React from 'react';
import {
  BookOpen,
  ArrowRight,
  Sparkles,
  Layers,
  Search,
  Award,
  AlertTriangle,
  FileCheck,
  Download,
  ShieldCheck,
  Cpu,
  Database,
  ExternalLink,
  TrendingUp,
  School,
  CheckCircle2,
  Check
} from 'lucide-react';

interface LandingPageProps {
  onGetStarted: () => void;
}

export const LandingPage: React.FC<LandingPageProps> = ({ onGetStarted }) => {
  const scrollToSection = (id: string) => {
    const el = document.getElementById(id);
    if (el) {
      el.scrollIntoView({ behavior: 'smooth' });
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 font-sans selection:bg-brand-500 selection:text-white flex flex-col">
      {/* 1. Global Navigation Bar */}
      <nav className="sticky top-0 z-50 bg-white/90 backdrop-blur-md border-b border-slate-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-brand-700 to-indigo-600 flex items-center justify-center text-white shadow-sm">
              <BookOpen className="w-5 h-5" />
            </div>
            <div>
              <div className="text-base font-bold tracking-tight text-slate-900 leading-tight">
                Curriculum Intelligence System
              </div>
              <div className="text-[11px] font-medium text-slate-500">
                Agentic Academic Course Engineering
              </div>
            </div>
          </div>

          <div className="hidden md:flex items-center gap-8 text-sm font-medium text-slate-600">
            <button
              onClick={() => scrollToSection('overview')}
              className="hover:text-brand-600 transition-colors"
            >
              Overview
            </button>
            <button
              onClick={() => scrollToSection('architecture')}
              className="hover:text-brand-600 transition-colors"
            >
              Five-Agent Architecture
            </button>
            <button
              onClick={() => scrollToSection('workflow')}
              className="hover:text-brand-600 transition-colors"
            >
              How It Works
            </button>
            <button
              onClick={() => scrollToSection('features')}
              className="hover:text-brand-600 transition-colors"
            >
              Features
            </button>
            <button
              onClick={() => scrollToSection('trust')}
              className="hover:text-brand-600 transition-colors"
            >
              Technology & Trust
            </button>
          </div>

          <div className="flex items-center gap-3">
            <button
              onClick={onGetStarted}
              className="inline-flex items-center gap-2 px-4 py-2 rounded-lg bg-brand-600 hover:bg-brand-700 text-white text-sm font-semibold shadow-xs hover:shadow-sm transition-all cursor-pointer"
            >
              <span>Get Started</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      </nav>

      {/* 2. Hero Section */}
      <section className="relative overflow-hidden bg-gradient-to-b from-white via-slate-50 to-slate-100 border-b border-slate-200 py-16 sm:py-24">
        {/* Subtle Background Glow */}
        <div className="absolute top-0 left-1/2 -translate-x-1/2 w-full max-w-7xl h-96 bg-gradient-to-tr from-indigo-100/40 via-brand-50/50 to-emerald-50/30 blur-3xl pointer-events-none -z-10" />

        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-3xl mx-auto">
            {/* Top Pill Badge */}
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-brand-50 border border-brand-200 text-brand-700 text-xs font-semibold uppercase tracking-wide mb-6">
              <Sparkles className="w-3.5 h-3.5 text-brand-600" />
              <span>AURA Multi-Agent Orchestration • Live Evidence Grounding</span>
            </div>

            {/* Headline */}
            <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold text-slate-900 tracking-tight leading-tight">
              Design Better Courses.{' '}
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-brand-600 to-indigo-600">
                Back Every Decision with Evidence.
              </span>
            </h1>

            {/* Subtitle */}
            <p className="mt-6 text-lg sm:text-xl text-slate-600 leading-relaxed font-normal">
              An AI-powered curriculum intelligence platform that connects institutional academic data,
              real industry requirements and leading university benchmarks to help faculty build
              better course plans.
            </p>

            {/* CTA Buttons */}
            <div className="mt-8 flex flex-col sm:flex-row items-center justify-center gap-4">
              <button
                onClick={onGetStarted}
                className="w-full sm:w-auto inline-flex items-center justify-center gap-2 px-6 py-3.5 rounded-xl bg-brand-600 hover:bg-brand-700 text-white text-base font-semibold shadow-md hover:shadow-lg transition-all cursor-pointer"
              >
                <span>Get Started</span>
                <ArrowRight className="w-5 h-5" />
              </button>

              <button
                onClick={() => scrollToSection('features')}
                className="w-full sm:w-auto inline-flex items-center justify-center gap-2 px-6 py-3.5 rounded-xl bg-white hover:bg-slate-50 text-slate-700 border border-slate-300 text-base font-semibold shadow-xs hover:shadow-sm transition-all cursor-pointer"
              >
                <span>Explore Features</span>
              </button>
            </div>

            <p className="mt-3 text-xs text-slate-500">
              No account creation or registration required for local development workspace.
            </p>
          </div>

          {/* Hero Interactive Preview Card */}
          <div className="mt-14 max-w-4xl mx-auto bg-white rounded-2xl border border-slate-200/90 shadow-xl overflow-hidden">
            <div className="bg-slate-900 text-white px-4 py-3 flex items-center justify-between border-b border-slate-800">
              <div className="flex items-center gap-2">
                <div className="w-3 h-3 rounded-full bg-rose-500" />
                <div className="w-3 h-3 rounded-full bg-amber-500" />
                <div className="w-3 h-3 rounded-full bg-emerald-500" />
                <span className="ml-2 text-xs font-mono text-slate-400">
                  aura-coordinator --session active --target="Agentic AI"
                </span>
              </div>
              <div className="flex items-center gap-2 text-xs font-mono text-emerald-400">
                <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
                <span>5 AGENTS SYNCHRONIZED</span>
              </div>
            </div>

            <div className="p-6 bg-slate-50 grid grid-cols-1 md:grid-cols-3 gap-4 text-left">
              <div className="p-4 bg-white rounded-xl border border-slate-200 shadow-2xs">
                <div className="text-[11px] font-bold uppercase text-brand-600 tracking-wider">
                  Phase 1: Concurrent Research
                </div>
                <div className="mt-2 text-sm font-semibold text-slate-900">
                  3 Agents Running in Parallel
                </div>
                <p className="mt-1 text-xs text-slate-500">
                  Curriculum Mapping + Industry Skills + Academic Benchmarking aggregate verifiable facts.
                </p>
              </div>

              <div className="p-4 bg-white rounded-xl border border-slate-200 shadow-2xs">
                <div className="text-[11px] font-bold uppercase text-indigo-600 tracking-wider">
                  Phase 2: Triangulation
                </div>
                <div className="mt-2 text-sm font-semibold text-slate-900">
                  Evidence Gap Analysis
                </div>
                <p className="mt-1 text-xs text-slate-500">
                  Triangulates syllabus vs. industry demands, assigning severity scores to missing competencies.
                </p>
              </div>

              <div className="p-4 bg-white rounded-xl border border-slate-200 shadow-2xs">
                <div className="text-[11px] font-bold uppercase text-emerald-600 tracking-wider">
                  Phase 3: Synthesis & Export
                </div>
                <div className="mt-2 text-sm font-semibold text-slate-900">
                  4 Proposals & KLE Tech PDF
                </div>
                <p className="mt-1 text-xs text-slate-500">
                  Recommendation Agent produces 4 distinct plans. Selected plan compiles to official DOCX/PDF.
                </p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* 3. Section: What the Platform Does */}
      <section id="overview" className="py-20 bg-white border-b border-slate-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-2xl mx-auto mb-16">
            <h2 className="text-xs font-bold uppercase tracking-widest text-brand-600">
              Platform Mission
            </h2>
            <h3 className="mt-2 text-3xl font-extrabold text-slate-900 tracking-tight">
              Everything Faculty Needs for Academic Excellence
            </h3>
            <p className="mt-4 text-base text-slate-600">
              Modern engineering demands agile, verified curricula. The Curriculum Intelligence
              System replaces guesswork with multi-agent research and evidence-based design.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            <div className="p-6 rounded-2xl bg-slate-50 border border-slate-200 hover:border-brand-200 hover:bg-brand-50/20 transition-all">
              <div className="w-12 h-12 rounded-xl bg-brand-100 text-brand-700 flex items-center justify-center mb-5">
                <School className="w-6 h-6" />
              </div>
              <h4 className="text-lg font-bold text-slate-900">Analyse Existing Curricula</h4>
              <p className="mt-2 text-sm text-slate-600 leading-relaxed">
                Parse institutional syllabi, Course Outcomes (COs), Program Outcomes (POs), Bloom's
                Taxonomy levels, and prerequisites from the institutional catalog.
              </p>
            </div>

            <div className="p-6 rounded-2xl bg-slate-50 border border-slate-200 hover:border-indigo-200 hover:bg-indigo-50/20 transition-all">
              <div className="w-12 h-12 rounded-xl bg-indigo-100 text-indigo-700 flex items-center justify-center mb-5">
                <TrendingUp className="w-6 h-6" />
              </div>
              <h4 className="text-lg font-bold text-slate-900">Research Real Industry Skills</h4>
              <p className="mt-2 text-sm text-slate-600 leading-relaxed">
                Retrieve actual employer skill requirements, active engineering job profiles, modern toolchains,
                and production competencies using live search grounding.
              </p>
            </div>

            <div className="p-6 rounded-2xl bg-slate-50 border border-slate-200 hover:border-purple-200 hover:bg-purple-50/20 transition-all">
              <div className="w-12 h-12 rounded-xl bg-purple-100 text-purple-700 flex items-center justify-center mb-5">
                <Award className="w-6 h-6" />
              </div>
              <h4 className="text-lg font-bold text-slate-900">Academic Benchmarking</h4>
              <p className="mt-2 text-sm text-slate-600 leading-relaxed">
                Benchmark courses against ACM/IEEE CS2023 curricula recommendations, IIT/NIT syllabi,
                and peer accredited university frameworks.
              </p>
            </div>

            <div className="p-6 rounded-2xl bg-slate-50 border border-slate-200 hover:border-amber-200 hover:bg-amber-50/20 transition-all">
              <div className="w-12 h-12 rounded-xl bg-amber-100 text-amber-700 flex items-center justify-center mb-5">
                <AlertTriangle className="w-6 h-6" />
              </div>
              <h4 className="text-lg font-bold text-slate-900">Identify Curriculum Gaps</h4>
              <p className="mt-2 text-sm text-slate-600 leading-relaxed">
                Automatically diagnose missing topics, outdated modules, and prerequisite deficits
                with clear severity scoring and verifiable evidence citations.
              </p>
            </div>

            <div className="p-6 rounded-2xl bg-slate-50 border border-slate-200 hover:border-teal-200 hover:bg-teal-50/20 transition-all">
              <div className="w-12 h-12 rounded-xl bg-teal-100 text-teal-700 flex items-center justify-center mb-5">
                <Layers className="w-6 h-6" />
              </div>
              <h4 className="text-lg font-bold text-slate-900">Generate 4 Course Alternatives</h4>
              <p className="mt-2 text-sm text-slate-600 leading-relaxed">
                Explore four distinct pedagogical perspectives: Industry-Oriented, Academic Depth,
                Balanced Standard, and Emerging Research.
              </p>
            </div>

            <div className="p-6 rounded-2xl bg-slate-50 border border-slate-200 hover:border-emerald-200 hover:bg-emerald-50/20 transition-all">
              <div className="w-12 h-12 rounded-xl bg-emerald-100 text-emerald-700 flex items-center justify-center mb-5">
                <FileCheck className="w-6 h-6" />
              </div>
              <h4 className="text-lg font-bold text-slate-900">KLE Tech Document Generation</h4>
              <p className="mt-2 text-sm text-slate-600 leading-relaxed">
                Export clean, audit-compliant Course Instruction Plans in official editable DOCX and
                pixel-perfect PDF formats, adhering strictly to university templates.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* 4. Section: Five-Agent Architecture */}
      <section id="architecture" className="py-20 bg-slate-50 border-b border-slate-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-2xl mx-auto mb-16">
            <h2 className="text-xs font-bold uppercase tracking-widest text-brand-600">
              System Intelligence
            </h2>
            <h3 className="mt-2 text-3xl font-extrabold text-slate-900 tracking-tight">
              Five Specialist Agents. Governed Execution.
            </h3>
            <p className="mt-4 text-base text-slate-600">
              The AURA Coordinator manages a staged workflow. The first three agents conduct research
              concurrently in parallel, followed sequentially by Gap Analysis and Proposal Synthesis.
            </p>
          </div>

          {/* Staged Visual Pipeline */}
          <div className="space-y-6">
            {/* Stage 1: Parallel Research Swarm */}
            <div className="p-6 bg-white rounded-2xl border border-slate-200 shadow-xs">
              <div className="flex items-center gap-3 mb-6">
                <span className="px-2.5 py-1 rounded-md bg-brand-100 text-brand-800 text-xs font-bold uppercase tracking-wider">
                  Stage 1: Parallel Research Swarm (Concurrent Execution)
                </span>
                <span className="text-xs text-slate-500">
                  Three independent specialist agents execute in parallel
                </span>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                {/* Agent 1 */}
                <div className="p-5 rounded-xl bg-slate-50 border border-slate-200">
                  <div className="flex items-center gap-2 mb-2">
                    <span className="w-6 h-6 rounded-full bg-brand-600 text-white text-xs flex items-center justify-center font-bold">
                      1
                    </span>
                    <h5 className="font-bold text-slate-900 text-sm">Curriculum Mapping Agent</h5>
                  </div>
                  <p className="text-xs text-slate-600 leading-relaxed">
                    Queries the academic catalog database to retrieve course metadata, prerequisites,
                    credit allocation (L-T-P), and outcome taxonomy vectors.
                  </p>
                  <div className="mt-3 text-[11px] font-medium text-brand-700 bg-brand-50 px-2 py-1 rounded">
                    Guardrail: Never infer unstated institutional outcomes.
                  </div>
                </div>

                {/* Agent 2 */}
                <div className="p-5 rounded-xl bg-slate-50 border border-slate-200">
                  <div className="flex items-center gap-2 mb-2">
                    <span className="w-6 h-6 rounded-full bg-indigo-600 text-white text-xs flex items-center justify-center font-bold">
                      2
                    </span>
                    <h5 className="font-bold text-slate-900 text-sm">Industry Skills Agent</h5>
                  </div>
                  <p className="text-xs text-slate-600 leading-relaxed">
                    Conducts live external research across engineering job postings, enterprise tech stacks,
                    and industry competency standards using search grounding.
                  </p>
                  <div className="mt-3 text-[11px] font-medium text-indigo-700 bg-indigo-50 px-2 py-1 rounded">
                    Guardrail: Cite exact URL, publisher, and year per finding.
                  </div>
                </div>

                {/* Agent 3 */}
                <div className="p-5 rounded-xl bg-slate-50 border border-slate-200">
                  <div className="flex items-center gap-2 mb-2">
                    <span className="w-6 h-6 rounded-full bg-purple-600 text-white text-xs flex items-center justify-center font-bold">
                      3
                    </span>
                    <h5 className="font-bold text-slate-900 text-sm">Academic Benchmarking Agent</h5>
                  </div>
                  <p className="text-xs text-slate-600 leading-relaxed">
                    Evaluates syllabus against ACM/IEEE Computer Science Curricula (CS2023), IIT syllabi,
                    and reputable peer university programs.
                  </p>
                  <div className="mt-3 text-[11px] font-medium text-purple-700 bg-purple-50 px-2 py-1 rounded">
                    Guardrail: Never fabricate compliance statements.
                  </div>
                </div>
              </div>
            </div>

            {/* Stage 2 & 3: Sequential Dependent Agents */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {/* Agent 4: Gap Analysis */}
              <div className="p-6 bg-white rounded-2xl border border-slate-200 shadow-xs">
                <div className="flex items-center gap-3 mb-4">
                  <span className="px-2.5 py-1 rounded-md bg-amber-100 text-amber-800 text-xs font-bold uppercase tracking-wider">
                    Stage 2: Triangulation
                  </span>
                  <span className="w-6 h-6 rounded-full bg-amber-600 text-white text-xs flex items-center justify-center font-bold">
                    4
                  </span>
                  <h5 className="font-bold text-slate-900 text-base">Gap Analysis Agent</h5>
                </div>
                <p className="text-sm text-slate-600 leading-relaxed">
                  Synthesizes upstream research from the first three agents. Compares current course
                  content against industry demands and academic benchmarks, surfacing missing topics,
                  underrepresented skills, and prerequisites with severity ratings.
                </p>
                <div className="mt-4 text-xs font-medium text-amber-800 bg-amber-50 px-3 py-1.5 rounded-lg border border-amber-200">
                  Blocks downstream proposal synthesis until all upstream research tasks successfully complete.
                </div>
              </div>

              {/* Agent 5: Recommendation */}
              <div className="p-6 bg-white rounded-2xl border border-slate-200 shadow-xs">
                <div className="flex items-center gap-3 mb-4">
                  <span className="px-2.5 py-1 rounded-md bg-emerald-100 text-emerald-800 text-xs font-bold uppercase tracking-wider">
                    Stage 3: Proposal Synthesis
                  </span>
                  <span className="w-6 h-6 rounded-full bg-emerald-600 text-white text-xs flex items-center justify-center font-bold">
                    5
                  </span>
                  <h5 className="font-bold text-slate-900 text-base">Recommendation Agent</h5>
                </div>
                <p className="text-sm text-slate-600 leading-relaxed">
                  Generates four genuinely distinct course-plan alternatives (Industry-Oriented, Academic
                  Depth, Balanced Standard, Emerging Research). Each proposal includes unit breakdowns,
                  contact hours, Course Outcomes, and verified evidence references.
                </p>
                <div className="mt-4 text-xs font-medium text-emerald-800 bg-emerald-50 px-3 py-1.5 rounded-lg border border-emerald-200">
                  Dynamically constructs alternatives directly from live research findings — zero hardcoded plans.
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* 5. Section: How It Works */}
      <section id="workflow" className="py-20 bg-white border-b border-slate-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-2xl mx-auto mb-16">
            <h2 className="text-xs font-bold uppercase tracking-widest text-brand-600">
              End-to-End Workflow
            </h2>
            <h3 className="mt-2 text-3xl font-extrabold text-slate-900 tracking-tight">
              From Faculty Request to Official Course Plan in Five Steps
            </h3>
            <p className="mt-4 text-base text-slate-600">
              A transparent, verifiable course engineering pipeline designed specifically for university faculty.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
            <div className="p-5 rounded-xl bg-slate-50 border border-slate-200 text-center relative">
              <div className="w-10 h-10 rounded-full bg-brand-600 text-white font-bold text-sm flex items-center justify-center mx-auto mb-4">
                1
              </div>
              <h5 className="font-bold text-slate-900 text-sm">Describe the Course</h5>
              <p className="mt-2 text-xs text-slate-500">
                Submit a natural language prompt specifying course title, target semester, and pedagogical focus.
              </p>
            </div>

            <div className="p-5 rounded-xl bg-slate-50 border border-slate-200 text-center relative">
              <div className="w-10 h-10 rounded-full bg-brand-600 text-white font-bold text-sm flex items-center justify-center mx-auto mb-4">
                2
              </div>
              <h5 className="font-bold text-slate-900 text-sm">AURA Coordination</h5>
              <p className="mt-2 text-xs text-slate-500">
                Coordinator locks credit constraints (L-T-P) and dispatches parallel specialist agents.
              </p>
            </div>

            <div className="p-5 rounded-xl bg-slate-50 border border-slate-200 text-center relative">
              <div className="w-10 h-10 rounded-full bg-brand-600 text-white font-bold text-sm flex items-center justify-center mx-auto mb-4">
                3
              </div>
              <h5 className="font-bold text-slate-900 text-sm">Real Research Retrieval</h5>
              <p className="mt-2 text-xs text-slate-500">
                Live evidence gathered from IEEE, ACM, industry job descriptions, and university catalogs.
              </p>
            </div>

            <div className="p-5 rounded-xl bg-slate-50 border border-slate-200 text-center relative">
              <div className="w-10 h-10 rounded-full bg-brand-600 text-white font-bold text-sm flex items-center justify-center mx-auto mb-4">
                4
              </div>
              <h5 className="font-bold text-slate-900 text-sm">Review 4 Alternatives</h5>
              <p className="mt-2 text-xs text-slate-500">
                Inspect 4 distinct course-plan proposals with module breakdowns, COs, and evidence citations.
              </p>
            </div>

            <div className="p-5 rounded-xl bg-slate-50 border border-slate-200 text-center relative">
              <div className="w-10 h-10 rounded-full bg-emerald-600 text-white font-bold text-sm flex items-center justify-center mx-auto mb-4">
                5
              </div>
              <h5 className="font-bold text-slate-900 text-sm">Official KLE Tech Plan</h5>
              <p className="mt-2 text-xs text-slate-500">
                Select your preferred plan and instantly download the institutional DOCX and PDF document.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* 6. Section: Platform Features */}
      <section id="features" className="py-20 bg-slate-50 border-b border-slate-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-2xl mx-auto mb-16">
            <h2 className="text-xs font-bold uppercase tracking-widest text-brand-600">
              Core Capabilities
            </h2>
            <h3 className="mt-2 text-3xl font-extrabold text-slate-900 tracking-tight">
              Enterprise Features Built for Academic Rigor
            </h3>
            <p className="mt-4 text-base text-slate-600">
              Every feature is implemented in code and verified against genuine institutional benchmarks.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="p-6 bg-white rounded-xl border border-slate-200 shadow-2xs">
              <div className="w-10 h-10 rounded-lg bg-brand-50 text-brand-600 flex items-center justify-center mb-4">
                <Database className="w-5 h-5" />
              </div>
              <h4 className="text-base font-bold text-slate-900">Institutional Curriculum Intelligence</h4>
              <p className="mt-2 text-xs text-slate-600 leading-relaxed">
                Seamless connectivity to PostgreSQL institutional catalog tables, extracting existing course codes,
                prerequisite graphs, and semester credit allocations.
              </p>
            </div>

            <div className="p-6 bg-white rounded-xl border border-slate-200 shadow-2xs">
              <div className="w-10 h-10 rounded-lg bg-indigo-50 text-indigo-600 flex items-center justify-center mb-4">
                <Search className="w-5 h-5" />
              </div>
              <h4 className="text-base font-bold text-slate-900">Industry Skills Research</h4>
              <p className="mt-2 text-xs text-slate-600 leading-relaxed">
                Retrieves verifiable industry skill trends and job requirements through Google Search Grounding
                with complete publisher metadata and dates.
              </p>
            </div>

            <div className="p-6 bg-white rounded-xl border border-slate-200 shadow-2xs">
              <div className="w-10 h-10 rounded-lg bg-purple-50 text-purple-600 flex items-center justify-center mb-4">
                <Award className="w-5 h-5" />
              </div>
              <h4 className="text-base font-bold text-slate-900">Academic Benchmarking</h4>
              <p className="mt-2 text-xs text-slate-600 leading-relaxed">
                Automated comparison against ACM CS2023 knowledge areas, IEEE curriculum guidelines,
                and syllabus structures from top technological institutes.
              </p>
            </div>

            <div className="p-6 bg-white rounded-xl border border-slate-200 shadow-2xs">
              <div className="w-10 h-10 rounded-lg bg-amber-50 text-amber-600 flex items-center justify-center mb-4">
                <AlertTriangle className="w-5 h-5" />
              </div>
              <h4 className="text-base font-bold text-slate-900">Evidence-Based Gap Analysis</h4>
              <p className="mt-2 text-xs text-slate-600 leading-relaxed">
                Objective triangulation of curriculum coverage. Surfaces high-severity deficits
                with clear pedagogical recommendations and supporting research links.
              </p>
            </div>

            <div className="p-6 bg-white rounded-xl border border-slate-200 shadow-2xs">
              <div className="w-10 h-10 rounded-lg bg-emerald-50 text-emerald-600 flex items-center justify-center mb-4">
                <Cpu className="w-5 h-5" />
              </div>
              <h4 className="text-base font-bold text-slate-900">Multi-Agent AI Orchestration</h4>
              <p className="mt-2 text-xs text-slate-600 leading-relaxed">
                AURA Coordinator manages task dispatch, dependency gates, state machines, and parallel
                execution threads with full audit-trail persistence.
              </p>
            </div>

            <div className="p-6 bg-white rounded-xl border border-slate-200 shadow-2xs">
              <div className="w-10 h-10 rounded-lg bg-teal-50 text-teal-600 flex items-center justify-center mb-4">
                <Layers className="w-5 h-5" />
              </div>
              <h4 className="text-base font-bold text-slate-900">Alternative Course Design</h4>
              <p className="mt-2 text-xs text-slate-600 leading-relaxed">
                Generates four distinct, non-duplicated curriculum philosophies so faculty can choose
                the optimal balance for their department's objectives.
              </p>
            </div>

            <div className="p-6 bg-white rounded-xl border border-slate-200 shadow-2xs">
              <div className="w-10 h-10 rounded-lg bg-blue-50 text-blue-600 flex items-center justify-center mb-4">
                <ExternalLink className="w-5 h-5" />
              </div>
              <h4 className="text-base font-bold text-slate-900">Evidence & Source Traceability</h4>
              <p className="mt-2 text-xs text-slate-600 leading-relaxed">
                Every recommended topic maps directly to an indexed evidence record with title, publisher,
                original URL, publication year, and retrieval timestamp.
              </p>
            </div>

            <div className="p-6 bg-white rounded-xl border border-slate-200 shadow-2xs">
              <div className="w-10 h-10 rounded-lg bg-brand-50 text-brand-600 flex items-center justify-center mb-4">
                <FileCheck className="w-5 h-5" />
              </div>
              <h4 className="text-base font-bold text-slate-900">KLE Tech Template Integration</h4>
              <p className="mt-2 text-xs text-slate-600 leading-relaxed">
                Directly utilizes the master institutional template, maintaining exact university logos,
                typography, margins, table structures, and department headers.
              </p>
            </div>

            <div className="p-6 bg-white rounded-xl border border-slate-200 shadow-2xs">
              <div className="w-10 h-10 rounded-lg bg-emerald-50 text-emerald-600 flex items-center justify-center mb-4">
                <Download className="w-5 h-5" />
              </div>
              <h4 className="text-base font-bold text-slate-900">Editable DOCX & PDF Export</h4>
              <p className="mt-2 text-xs text-slate-600 leading-relaxed">
                Download fully formatted, editable Word (.docx) files and pixel-perfect native PDFs
                generated via Microsoft Word COM engine on Windows.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* 7. Section: Technology and Trust */}
      <section id="trust" className="py-20 bg-white border-b border-slate-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-2xl mx-auto mb-16">
            <h2 className="text-xs font-bold uppercase tracking-widest text-brand-600">
              Technology & Trust
            </h2>
            <h3 className="mt-2 text-3xl font-extrabold text-slate-900 tracking-tight">
              Grounded in Evidence. Built on Proven Engineering.
            </h3>
            <p className="mt-4 text-base text-slate-600">
              We only cite technologies and capabilities that are genuinely implemented in our codebase.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-8 items-center">
            {/* Tech Stack List */}
            <div className="space-y-4">
              <h4 className="text-lg font-bold text-slate-900 mb-2">Implemented Architecture</h4>
              
              <div className="flex items-start gap-3 p-3.5 rounded-xl bg-slate-50 border border-slate-200">
                <Check className="w-5 h-5 text-emerald-600 flex-shrink-0 mt-0.5" />
                <div>
                  <div className="text-sm font-semibold text-slate-900">Spring Boot 3.4.2 & Java 21</div>
                  <div className="text-xs text-slate-500">Robust backend microservice architecture with Flyway migrations.</div>
                </div>
              </div>

              <div className="flex items-start gap-3 p-3.5 rounded-xl bg-slate-50 border border-slate-200">
                <Check className="w-5 h-5 text-emerald-600 flex-shrink-0 mt-0.5" />
                <div>
                  <div className="text-sm font-semibold text-slate-900">React 18 & TypeScript with Vite</div>
                  <div className="text-xs text-slate-500">Modern, reactive single-page client with Tailwind CSS design system.</div>
                </div>
              </div>

              <div className="flex items-start gap-3 p-3.5 rounded-xl bg-slate-50 border border-slate-200">
                <Check className="w-5 h-5 text-emerald-600 flex-shrink-0 mt-0.5" />
                <div>
                  <div className="text-sm font-semibold text-slate-900">PostgreSQL 18.6 Database</div>
                  <div className="text-xs text-slate-500">Relational persistence for institutional catalog, evidence, and audit logs.</div>
                </div>
              </div>

              <div className="flex items-start gap-3 p-3.5 rounded-xl bg-slate-50 border border-slate-200">
                <Check className="w-5 h-5 text-emerald-600 flex-shrink-0 mt-0.5" />
                <div>
                  <div className="text-sm font-semibold text-slate-900">AURA Agent Orchestration Engine</div>
                  <div className="text-xs text-slate-500">Multi-agent coordinator enforcing strict task dependencies and guardrails.</div>
                </div>
              </div>

              <div className="flex items-start gap-3 p-3.5 rounded-xl bg-slate-50 border border-slate-200">
                <Check className="w-5 h-5 text-emerald-600 flex-shrink-0 mt-0.5" />
                <div>
                  <div className="text-sm font-semibold text-slate-900">Groq LLM Integration (gpt-oss-120b)</div>
                  <div className="text-xs text-slate-500">High-speed reasoning for proposal drafting and gap analysis.</div>
                </div>
              </div>

              <div className="flex items-start gap-3 p-3.5 rounded-xl bg-slate-50 border border-slate-200">
                <Check className="w-5 h-5 text-emerald-600 flex-shrink-0 mt-0.5" />
                <div>
                  <div className="text-sm font-semibold text-slate-900">Google Search Grounding & Jina AI Embeddings</div>
                  <div className="text-xs text-slate-500">Live web research and 768-dimensional hybrid semantic vector search.</div>
                </div>
              </div>
            </div>

            {/* Institutional Trust Statement Card */}
            <div className="p-8 rounded-2xl bg-gradient-to-br from-slate-900 to-brand-950 text-white shadow-xl">
              <div className="w-12 h-12 rounded-xl bg-brand-500/20 border border-brand-400/30 flex items-center justify-center text-brand-300 mb-6">
                <ShieldCheck className="w-6 h-6" />
              </div>
              <h4 className="text-xl font-bold tracking-tight">Institutional Trust Guarantee</h4>
              <blockquote className="mt-4 text-base text-slate-300 italic leading-relaxed border-l-2 border-brand-500 pl-4">
                "Recommendations are grounded in retrieved evidence. Missing academic information is not
                silently invented. Faculty retains control over final course approval."
              </blockquote>
              <div className="mt-6 space-y-2 text-xs text-slate-400">
                <div className="flex items-center gap-2">
                  <CheckCircle2 className="w-4 h-4 text-emerald-400" />
                  <span>Zero fabricated course codes, credits, or assessment percentages.</span>
                </div>
                <div className="flex items-center gap-2">
                  <CheckCircle2 className="w-4 h-4 text-emerald-400" />
                  <span>Auditable provenance for all recommended textbooks and web references.</span>
                </div>
                <div className="flex items-center gap-2">
                  <CheckCircle2 className="w-4 h-4 text-emerald-400" />
                  <span>Immutable audit trail tracking faculty review decisions.</span>
                </div>
              </div>

              <div className="mt-8 pt-6 border-t border-slate-800 flex items-center justify-between">
                <span className="text-xs text-slate-400">Ready to design your course?</span>
                <button
                  onClick={onGetStarted}
                  className="px-4 py-2 rounded-lg bg-brand-600 hover:bg-brand-500 text-white text-xs font-semibold shadow-xs transition-all cursor-pointer"
                >
                  Enter Workspace
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* 8. Global Footer */}
      <footer className="mt-auto bg-slate-900 text-slate-400 text-xs py-12 border-t border-slate-800">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-8 mb-8">
            <div className="md:col-span-2">
              <div className="flex items-center gap-2 text-white text-base font-bold mb-3">
                <BookOpen className="w-5 h-5 text-brand-400" />
                <span>Curriculum Intelligence System</span>
              </div>
              <p className="text-slate-400 text-xs leading-relaxed max-w-md">
                An Agentic AI-powered academic curriculum design and intelligence platform developed for
                higher educational institutions, incorporating real-world research, evidence-grounded
                benchmarking, and institutional course-plan document compilation.
              </p>
              <div className="mt-4 text-[11px] text-slate-500">
                Institutional Framework: KLE Technological University, Belagavi
              </div>
            </div>

            <div>
              <div className="text-white font-semibold text-xs tracking-wider uppercase mb-3">
                Navigation
              </div>
              <ul className="space-y-2">
                <li>
                  <button onClick={() => scrollToSection('overview')} className="hover:text-white transition-colors">
                    Platform Mission
                  </button>
                </li>
                <li>
                  <button onClick={() => scrollToSection('architecture')} className="hover:text-white transition-colors">
                    Five-Agent Architecture
                  </button>
                </li>
                <li>
                  <button onClick={() => scrollToSection('workflow')} className="hover:text-white transition-colors">
                    How It Works
                  </button>
                </li>
                <li>
                  <button onClick={() => scrollToSection('features')} className="hover:text-white transition-colors">
                    Features
                  </button>
                </li>
                <li>
                  <button onClick={() => scrollToSection('trust')} className="hover:text-white transition-colors">
                    Technology & Trust
                  </button>
                </li>
              </ul>
            </div>

            <div>
              <div className="text-white font-semibold text-xs tracking-wider uppercase mb-3">
                Workspace
              </div>
              <ul className="space-y-2">
                <li>
                  <button onClick={onGetStarted} className="text-brand-400 hover:text-brand-300 font-semibold flex items-center gap-1">
                    <span>Enter Chat Workspace</span>
                    <ArrowRight className="w-3.5 h-3.5" />
                  </button>
                </li>
                <li className="text-[11px] text-slate-500 mt-2">
                  Local Execution: Spring Boot (8081) • Vite Client (5173) • PostgreSQL (5432)
                </li>
              </ul>
            </div>
          </div>

          <div className="pt-8 border-t border-slate-800 flex flex-col sm:flex-row items-center justify-between text-[11px] text-slate-500">
            <div>
              © 2026 Curriculum Intelligence System. All rights reserved.
            </div>
            <div className="mt-2 sm:mt-0">
              Academic Course Plan Engineering Platform
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
};
