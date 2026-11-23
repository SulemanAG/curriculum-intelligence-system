package org.curriculumai.coordinator.dto;

import org.curriculumai.coordinator.RunStage;

import java.util.List;

public record StageResponse(
        String id,
        String label,
        String status,
        String message,
        Integer elapsedSeconds,
        List<ParallelAgentResponse> parallelAgents
) {
    public static StageResponse fromEntity(RunStage stage) {
        List<ParallelAgentResponse> agentResponses = (stage.getParallelAgents() != null && !stage.getParallelAgents().isEmpty())
                ? stage.getParallelAgents().stream().map(ParallelAgentResponse::fromEntity).toList()
                : null;

        return new StageResponse(
                stage.getStageId(),
                stage.getLabel(),
                stage.getStatus(),
                stage.getMessage(),
                stage.getElapsedSeconds(),
                agentResponses
        );
    }
}

