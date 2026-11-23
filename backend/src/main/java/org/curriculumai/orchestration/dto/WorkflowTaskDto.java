package org.curriculumai.orchestration.dto;

import org.curriculumai.orchestration.domain.TaskStatus;
import org.curriculumai.orchestration.domain.WorkflowTask;

import java.time.Instant;

public record WorkflowTaskDto(
        String id,
        String runId,
        String taskKey,
        String title,
        String assignedAgentId,
        TaskStatus status,
        int sequenceOrder,
        boolean parallelEligible,
        boolean required,
        int attemptCount,
        int maxRetries,
        int timeoutSeconds,
        String inputPayloadJson,
        String outputPayloadJson,
        String errorMessage,
        String errorType,
        Instant startedAt,
        Instant completedAt,
        Instant createdAt
) {
    public static WorkflowTaskDto fromEntity(WorkflowTask task) {
        return new WorkflowTaskDto(
                task.getId(),
                task.getRunId(),
                task.getTaskKey(),
                task.getTitle(),
                task.getAssignedAgentId(),
                task.getStatus(),
                task.getSequenceOrder(),
                task.isParallelEligible(),
                task.isRequired(),
                task.getAttemptCount(),
                task.getMaxRetries(),
                task.getTimeoutSeconds(),
                task.getInputPayloadJson(),
                task.getOutputPayloadJson(),
                task.getErrorMessage(),
                task.getErrorType(),
                task.getStartedAt(),
                task.getCompletedAt(),
                task.getCreatedAt()
        );
    }
}

