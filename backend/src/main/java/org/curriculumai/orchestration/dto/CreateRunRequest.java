package org.curriculumai.orchestration.dto;

import jakarta.validation.constraints.NotBlank;
import org.curriculumai.orchestration.domain.WorkflowType;

public record CreateRunRequest(
        @NotBlank(message = "Prompt must not be blank")
        String prompt,

        @NotBlank(message = "Subject must not be blank")
        String subject,

        WorkflowType workflowType,

        String conversationId,
        String messageId,
        String departmentId,
        String institutionId,
        String programmeId,
        String curriculumVersionId,
        String courseCode
) {
    public CreateRunRequest(String prompt, String subject, WorkflowType workflowType,
                            String conversationId, String messageId, String departmentId, String institutionId) {
        this(prompt, subject, workflowType, conversationId, messageId, departmentId, institutionId, null, null, null);
    }

    public CreateRunRequest {
        if (workflowType == null) {
            workflowType = WorkflowType.CURRICULUM_RESEARCH_AND_PLANNING;
        }
    }
}

