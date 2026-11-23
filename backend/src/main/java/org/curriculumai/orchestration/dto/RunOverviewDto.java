package org.curriculumai.orchestration.dto;

import org.curriculumai.orchestration.domain.OrchestrationRun;
import org.curriculumai.orchestration.domain.RunStatus;
import org.curriculumai.orchestration.domain.WorkflowType;

import java.time.Instant;

public record RunOverviewDto(
        String id,
        String userId,
        String conversationId,
        String messageId,
        String rawPrompt,
        String subject,
        WorkflowType workflowType,
        RunStatus status,
        int totalTasks,
        int completedTasks,
        int failedTasks,
        String currentCheckpoint,
        String errorMessage,
        Instant startTime,
        Instant endTime,
        Instant createdAt,
        Instant updatedAt
) {
    public static RunOverviewDto fromEntity(OrchestrationRun run) {
        return new RunOverviewDto(
                run.getId(),
                run.getUserId(),
                run.getConversationId(),
                run.getMessageId(),
                run.getRawPrompt(),
                run.getSubject(),
                run.getWorkflowType(),
                run.getStatus(),
                run.getTotalTasks(),
                run.getCompletedTasks(),
                run.getFailedTasks(),
                run.getCurrentCheckpoint(),
                run.getErrorMessage(),
                run.getStartTime(),
                run.getEndTime(),
                run.getCreatedAt(),
                run.getUpdatedAt()
        );
    }
}

