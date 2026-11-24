/**
 * Curriculum Intelligence System - Centralized API Service Client
 * 
 * ARCHITECTURE NOTICE:
 * This client defines the PROPOSED INTEGRATION CONTRACTS for the Phase 2 Java 21 / Spring Boot 3 backend.
 * Endpoints documented below represent the target service contracts specified in Chapter 5
 * and the 14-Lab Build Book. In Phase 1 (Frontend Foundation), backend calls report offline/unreachable
 * status when the Spring Boot service is not running. No mock/fabricated responses are presented as live data.
 * 
 * Configuration is handled via environment variable VITE_API_BASE_URL (defaults to http://localhost:8081/api).
 * No secret tokens, credentials, or API keys are embedded or exposed in frontend code.
 */

import {
  ApiResponse,
  CourseRequest,
  ResearchRun,
  CoursePlanAlternative,
  UserFeedback,
  FinalCoursePlan,
  ValidationReport,
  LlmStatus
} from '../types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081/api';

class ApiClient {
  private baseUrl: string;

  constructor(baseUrl: string) {
    this.baseUrl = baseUrl.replace(/\/+$/, '');
  }

  public getBaseUrl(): string {
    return this.baseUrl;
  }

  /**
   * Health Check Contract
   * @contract GET /api/health or /actuator/health
   * @description Verifies connectivity with the Spring Boot Coordinator runtime
   * @status Proposed integration endpoint (Phase 2 backend required)
   */
  async checkBackendHealth(): Promise<{ online: boolean; message: string }> {
    try {
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), 2000);
      
      const response = await fetch(`${this.baseUrl}/health`, {
        signal: controller.signal,
        headers: { 'Accept': 'application/json' }
      });
      clearTimeout(timeoutId);
      
      if (response.ok) {
        return { online: true, message: 'Backend Spring Boot Coordinator connected and operational.' };
      }
      return { online: false, message: `Backend service responded with HTTP status ${response.status}` };
    } catch {
      return {
        online: false,
        message: `Backend service not reachable at ${this.baseUrl}. Spring Boot backend service is required for live research.`
      };
    }
  }

  /**
   * LLM Status & Failover Health Contract
   * @contract GET /api/curriculum/llm/status
   * @description Retrieves active LLM provider, failover telemetry, and circuit breaker status
   */
  async getLlmStatus(): Promise<ApiResponse<LlmStatus>> {
    try {
      const response = await fetch(`${this.baseUrl}/curriculum/llm/status`, {
        headers: { 'Accept': 'application/json' }
      });
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }
      return await response.json();
    } catch (err: unknown) {
      return {
        success: false,
        message: err instanceof Error ? err.message : 'Unable to retrieve LLM status',
        data: undefined,
        timestamp: new Date().toISOString()
      };
    }
  }

  /**
   * Course Request Ingestion Contract
   * @contract POST /api/curriculum/requests
   * @description Submits a natural-language course design request to the AURA Coordinator
   * @payload { rawPrompt: string, department: string, degreeLevel: 'UG' | 'PG' }
   * @returns ApiResponse<CourseRequest>
   * @status Proposed integration contract (Phase 2 backend required)
   */
  async submitCourseRequest(
    prompt: string,
    department: string = 'Computer Science & Engineering'
  ): Promise<ApiResponse<CourseRequest>> {
    try {
      const response = await fetch(`${this.baseUrl}/curriculum/requests`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        body: JSON.stringify({
          rawPrompt: prompt,
          department,
          degreeLevel: 'UG'
        })
      });

      if (!response.ok) {
        throw new Error(`Server returned HTTP ${response.status}: ${response.statusText}`);
      }

      const data: ApiResponse<CourseRequest> = await response.json();
      return data;
    } catch (err: unknown) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown connection error';
      return {
        success: false,
        error: errorMessage,
        message: `Backend service at ${this.baseUrl} is unavailable. Live multi-agent research requires the Spring Boot backend.`,
        timestamp: new Date().toISOString()
      };
    }
  }

  /**
   * Research Run Execution Status Polling Contract
   * @contract GET /api/curriculum/runs/:runId
   * @description Polls the execution progress of the Coordinator and parallel workers (Lab 7/8)
   * @returns ApiResponse<ResearchRun>
   * @status Proposed integration contract (Phase 2 backend required)
   */
  async getResearchRun(runId: string): Promise<ApiResponse<ResearchRun>> {
    try {
      const response = await fetch(`${this.baseUrl}/curriculum/runs/${encodeURIComponent(runId)}`, {
        headers: { 'Accept': 'application/json' }
      });

      if (!response.ok) {
        throw new Error(`Server returned HTTP ${response.status}: ${response.statusText}`);
      }

      const data: ApiResponse<ResearchRun> = await response.json();
      return data;
    } catch (err: unknown) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to retrieve research run status';
      return {
        success: false,
        error: errorMessage,
        message: 'Could not fetch live research progress from backend.',
        timestamp: new Date().toISOString()
      };
    }
  }

  /**
   * Course Plan Alternatives Retrieval Contract
   * @contract GET /api/curriculum/runs/:runId/alternatives
   * @description Fetches exactly four generated, validated course-plan proposals
   * @returns ApiResponse<CoursePlanAlternative[]>
   * @status Proposed integration contract (Phase 2 backend required)
   */
  async getCourseAlternatives(runId: string): Promise<ApiResponse<CoursePlanAlternative[]>> {
    try {
      const response = await fetch(`${this.baseUrl}/curriculum/runs/${encodeURIComponent(runId)}/alternatives`, {
        headers: { 'Accept': 'application/json' }
      });

      if (!response.ok) {
        throw new Error(`Server returned HTTP ${response.status}: ${response.statusText}`);
      }

      const data: ApiResponse<CoursePlanAlternative[]> = await response.json();
      return data;
    } catch (err: unknown) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to retrieve course alternatives';
      return {
        success: false,
        error: errorMessage,
        message: 'No alternatives could be retrieved from the backend API.',
        timestamp: new Date().toISOString()
      };
    }
  }

  /**
   * Human Preference & Feedback Recording Contract
   * @contract POST /api/curriculum/feedback
   * @description Commits faculty alternative selection and rationale to the preference dataset (Lab 12)
   * @payload UserFeedback
   * @returns ApiResponse<{ finalPlanId: string }>
   * @status Proposed integration contract (Phase 2 backend required)
   */
  async submitSelectionFeedback(feedback: UserFeedback): Promise<ApiResponse<{ finalPlanId: string }>> {
    try {
      const response = await fetch(`${this.baseUrl}/curriculum/feedback`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        body: JSON.stringify(feedback)
      });

      if (!response.ok) {
        throw new Error(`Server returned HTTP ${response.status}: ${response.statusText}`);
      }

      const data: ApiResponse<{ finalPlanId: string }> = await response.json();
      return data;
    } catch (err: unknown) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to submit selection feedback';
      return {
        success: false,
        error: errorMessage,
        message: 'Selection feedback could not be committed to the backend.',
        timestamp: new Date().toISOString()
      };
    }
  }

  /**
   * Final Course Plan Retrieval Contract
   * @contract GET /api/curriculum/plans/:planId
   * @description Retrieves the assembled institutional course-plan document
   * @returns ApiResponse<FinalCoursePlan>
   * @status Proposed integration contract (Phase 2 backend required)
   */
  async getFinalCoursePlan(planId: string): Promise<ApiResponse<FinalCoursePlan>> {
    try {
      const response = await fetch(`${this.baseUrl}/curriculum/plans/${encodeURIComponent(planId)}`, {
        headers: { 'Accept': 'application/json' }
      });

      if (!response.ok) {
        throw new Error(`Server returned HTTP ${response.status}: ${response.statusText}`);
      }

      const data: ApiResponse<FinalCoursePlan> = await response.json();
      return data;
    } catch (err: unknown) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to retrieve final course plan';
      return {
        success: false,
        error: errorMessage,
        message: 'Final course plan document could not be retrieved.',
        timestamp: new Date().toISOString()
      };
    }
  }

  /**
   * Acceptance Validation Report Contract
   * @contract GET /api/curriculum/runs/:runId/validation
   * @description Fetches automated test report evaluating evidence citations and CO/PO consistency (Lab 11)
   * @returns ApiResponse<ValidationReport>
   * @status Proposed integration contract (Phase 2 backend required)
   */
  async getValidationReport(runId: string): Promise<ApiResponse<ValidationReport>> {
    try {
      const response = await fetch(`${this.baseUrl}/curriculum/runs/${encodeURIComponent(runId)}/validation`, {
        headers: { 'Accept': 'application/json' }
      });

      if (!response.ok) {
        throw new Error(`Server returned HTTP ${response.status}: ${response.statusText}`);
      }

      const data: ApiResponse<ValidationReport> = await response.json();
      return data;
    } catch (err: unknown) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to retrieve validation report';
      return {
        success: false,
        error: errorMessage,
        message: 'Validation report unavailable from backend.',
        timestamp: new Date().toISOString()
      };
    }
  }

  /**
   * Institutional PDF Export Contract
   * @contract GET /api/curriculum/plans/:planId/pdf
   * @description Streams the generated institutional PDF course plan document
   * @returns Blob
   */
  async downloadCoursePlanPdf(planId: string): Promise<{ blob?: Blob; error?: string }> {
    try {
      const response = await fetch(`${this.baseUrl}/curriculum/plans/${encodeURIComponent(planId)}/pdf`, {
        headers: { 'Accept': 'application/pdf' }
      });

      if (!response.ok) {
        throw new Error(`PDF generation endpoint returned HTTP ${response.status}: ${response.statusText}`);
      }

      const blob = await response.blob();
      return { blob };
    } catch (err: unknown) {
      const errorMessage = err instanceof Error ? err.message : 'PDF export service unavailable';
      return { error: errorMessage };
    }
  }

  /**
   * Institutional DOCX Export Contract
   * @contract GET /api/curriculum/plans/:planId/docx
   * @description Streams the generated institutional editable Word (.docx) course plan document
   * @returns Blob
   */
  async downloadCoursePlanDocx(planId: string): Promise<{ blob?: Blob; error?: string }> {
    try {
      const response = await fetch(`${this.baseUrl}/curriculum/plans/${encodeURIComponent(planId)}/docx`, {
        headers: { 'Accept': 'application/vnd.openxmlformats-officedocument.wordprocessingml.document' }
      });

      if (!response.ok) {
        throw new Error(`DOCX generation endpoint returned HTTP ${response.status}: ${response.statusText}`);
      }

      const blob = await response.blob();
      return { blob };
    } catch (err: unknown) {
      const errorMessage = err instanceof Error ? err.message : 'DOCX export service unavailable';
      return { error: errorMessage };
    }
  }
}

export const apiClient = new ApiClient(API_BASE_URL);
