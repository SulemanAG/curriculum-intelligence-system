/**
 * Domain Models & TypeScript Interfaces for Curriculum Intelligence System
 * Academic Technology Project at KLE Technological University
 * Designed according to Chapter 5 (Curriculum Intelligence System) & 14-Lab specifications.
 */

export type ExecutionStatus = 'pending' | 'running' | 'completed' | 'failed' | 'awaiting_human_input';

export type DegreeLevel = 'UG' | 'PG';

export interface CourseRequest {
  id: string;
  rawPrompt: string;
  courseTitle?: string;
  department: string;
  degreeLevel: DegreeLevel;
  semester?: number;
  status: 'submitted' | 'processing' | 'completed' | 'failed';
  submittedAt: string;
}

export type ResearchStageId =
  | 'intake_analysis'
  | 'parallel_view_building'
  | 'gap_analysis'
  | 'recommendation_drafting'
  | 'validation_acceptance'
  | 'human_review_gate'
  | 'governance_publish';

export interface ParallelAgentTask {
  id: string;
  name: string;
  role: string;
  status: ExecutionStatus;
  message: string;
  guardrail: string;
  startedAt?: string;
  completedAt?: string;
  evidenceItemsCount?: number;
}

export interface ResearchStage {
  id: ResearchStageId;
  name: string;
  description: string;
  status: ExecutionStatus;
  isParallel?: boolean;
  message: string;
  startedAt?: string;
  completedAt?: string;
  parallelAgents?: ParallelAgentTask[];
}

export interface ResearchRun {
  id: string;
  requestId: string;
  prompt: string;
  currentStageId: ResearchStageId;
  status: ExecutionStatus;
  stages: ResearchStage[];
  startedAt: string;
  completedAt?: string;
  elapsedSeconds?: number;
  validationPassed?: boolean;
  auditTrailId: string;
  error?: string;
}

export type EvidenceSourceType =
  | 'acm_standard'
  | 'apni_guideline'
  | 'iit_nit_curriculum'
  | 'industry_report'
  | 'job_posting'
  | 'alumni_feedback';

export interface EvidenceReference {
  id: string;
  title: string;
  institutionOrPublisher: string;
  sourceType: EvidenceSourceType;
  originalUrl?: string;
  publicationDate?: string;
  retrievalTimestamp: string;
  supportedFinding: string;
  clauseReference?: string;
  isVerified: boolean;
  confidenceScore?: number;
}

export interface InstitutionalBenchmark {
  id: string;
  institutionName: string; // e.g. "IIT Bombay", "IIT Madras", "NIT Surathkal"
  courseCode?: string;
  courseTitle: string;
  clauseOrTopic: string;
  alignment: 'aligned' | 'partially_aligned' | 'missing';
  evidenceRefId: string;
}

export interface IndustrySkillEvidence {
  id: string;
  skillName: string;
  demandCategory: 'Core Essential' | 'Specialized Tooling' | 'Emerging Technology';
  citations: string[];
  evidenceRefId: string;
}

export interface CurriculumGap {
  id: string;
  topic: string;
  gapType: 'missing_topic' | 'outdated_content' | 'skill_mismatch';
  severity: 'critical' | 'moderate' | 'low';
  evidenceDescription: string;
  evidenceRefId: string;
}

export interface CourseOutcome {
  code: string; // e.g., "CO1", "CO2"
  statement: string;
  bloomsTaxonomyLevel: 'Remembering' | 'Understanding' | 'Applying' | 'Analyzing' | 'Evaluating' | 'Creating';
  poMapping: string[]; // e.g., ["PO1", "PO2", "PSO1"]
}

export interface CourseModule {
  moduleNumber: number;
  title: string;
  suggestedHours: number;
  topics: string[];
  learningOutcomes: string[];
  supportingEvidenceIds: string[];
}

export interface AssessmentScheme {
  ciePercentage?: number; // Continuous Internal Evaluation percentage (configurable; not hardcoded)
  seePercentage?: number; // Semester End Examination percentage (configurable; not hardcoded)
  evaluationFrameworkName?: string; // Descriptive name for institutional evaluation framework
  components: {
    name: string;
    weightage: number;
    description: string;
  }[];
}

export interface CoursePlanAlternative {
  id: string;
  code: 'ALT-1' | 'ALT-2' | 'ALT-3' | 'ALT-4';
  title: string;
  strategyName: string;
  tagline: string;
  curriculumPhilosophy: string;
  credits: number;
  lectureHours: number;
  tutorialHours: number;
  practicalHours: number;
  totalHours: number;
  courseOutcomes: CourseOutcome[];
  modules: CourseModule[];
  keyHighlights: string[];
  industryAlignmentSummary: string;
  institutionalBenchmarkSummary: string;
  evidenceCoveragePercent: number;
  evidenceReferences: EvidenceReference[];
  assessmentScheme: AssessmentScheme;
  isRecommendedStrategy?: boolean;
}

export interface UserFeedback {
  requestId: string;
  selectedAlternativeId: string;
  rejectedAlternativeIds: string[];
  facultyId?: string;
  facultyName?: string;
  rationale: string;
  suggestedModifications?: string;
  submittedAt: string;
}

export interface ValidationCheck {
  ruleId: string;
  description: string;
  passed: boolean;
  message: string;
  evidenceCited: boolean;
}

export interface ValidationReport {
  passed: boolean;
  timestamp: string;
  checks: ValidationCheck[];
  failingConstraints: string[];
  remedyActionTaken?: string;
}

export interface FinalCoursePlan {
  id: string;
  requestId: string;
  selectedAlternativeId: string;
  courseCode: string;
  courseTitle: string;
  department: string;
  degreeLevel: DegreeLevel;
  semester: number;
  credits: number;
  contactHours: {
    lecture: number;
    tutorial: number;
    practical: number;
    total: number;
  };
  courseDescription: string;
  prerequisites: string[];
  courseOutcomes: CourseOutcome[];
  modules: CourseModule[];
  assessmentScheme: AssessmentScheme;
  prescribedTextbooks: string[];
  referenceMaterials: string[];
  industryAlignmentNotes: string;
  institutionalBenchmarkNotes: string;
  evidenceReferences: EvidenceReference[];
  approvalStatus: 'draft' | 'under_review' | 'approved' | 'published';
  version: string;
  generatedAt: string;
  auditTrailId: string;
  pdfDownloadUrl?: string;
  templateSpecificationNotes?: string;
  signOffWorkflowNotes?: string;
}

export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: string;
  message?: string;
  timestamp: string;
}

// ==========================================
// Conversational AI Workspace Types
// ==========================================

export type NavigationDestination =
  | 'chat'
  | 'analyze-agents'
  | 'library'
  | 'scheduled'
  | 'other-tools'
  | 'profile';

export type MessageRole = 'user' | 'assistant' | 'system';

export type MessagePayloadType =
  | 'text'
  | 'research_progress'
  | 'clarification'
  | 'alternatives'
  | 'feedback'
  | 'final_document';

export interface ChatMessage {
  id: string;
  role: MessageRole;
  content: string;
  timestamp: string;
  payloadType?: MessagePayloadType;
  researchRun?: ResearchRun;
  alternatives?: CoursePlanAlternative[];
  selectedAlternativeId?: string;
  finalPlan?: FinalCoursePlan;
  isDevPreview?: boolean;
}

export interface Conversation {
  id: string;
  title: string;
  isCustomTitle?: boolean;
  messages: ChatMessage[];
  createdAt: string;
  updatedAt: string;
}

export interface LibraryDocument {
  id: string;
  title: string;
  courseTitle: string;
  department: string;
  generatedDate: string;
  fileSize?: string;
  downloadUrl?: string;
  isAvailable: boolean;
}

export interface ScheduledTask {
  id: string;
  title: string;
  frequency: string;
  nextRunDate: string;
  targetDepartment: string;
  status: 'active' | 'paused' | 'completed';
}

export interface LlmProviderDetail {
  providerName: string;
  modelName: string;
  configured: boolean;
  circuitState: 'CLOSED' | 'OPEN' | 'HALF_OPEN';
  consecutiveFailures: number;
  lastSuccess?: string | null;
  lastFailure?: string | null;
  lastHttpStatus?: number;
  lastErrorCategory?: string | null;
  totalRequests: number;
  totalSuccesses: number;
  totalFailures: number;
}

export interface LlmExecutionDetail {
  providerName: string;
  modelName: string;
  failoverOccurred: boolean;
  failoverFrom?: string | null;
  statusCode: number;
  durationMs: number;
}

export interface LlmStatus {
  primaryProvider: string;
  activeProvider: string;
  activeModel: string;
  available: boolean;
  lastExecution?: LlmExecutionDetail;
  providers: Record<string, LlmProviderDetail>;
}

