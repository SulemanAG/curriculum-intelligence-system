package org.curriculumai.orchestration.dto;

import org.curriculumai.orchestration.domain.WorkflowEvent;
import org.curriculumai.orchestration.domain.WorkflowEventType;

import java.time.Instant;

public record WorkflowEventDto(
        String id,
        String runId,
        String taskId,
        String agentId,
        WorkflowEventType eventType,
        String eventSummary,
        String metadataJson,
        Instant createdAt
) {
    public static WorkflowEventDto fromEntity(WorkflowEvent event) {
        return new WorkflowEventDto(
                event.getId(),
                event.getRunId(),
                event.getTaskId(),
                event.getAgentId(),
                event.getEventType(),
                event.getEventSummary(),
                event.getMetadataJson(),
                event.getCreatedAt()
        );
    }
}

