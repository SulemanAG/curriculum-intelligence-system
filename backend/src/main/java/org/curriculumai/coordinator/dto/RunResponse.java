package org.curriculumai.coordinator.dto;

import org.curriculumai.coordinator.AnalysisRun;

import java.util.List;

public record RunResponse(
        String id,
        String status,
        String currentStageId,
        List<StageResponse> stages,
        Integer elapsedSeconds,
        Boolean hasAlternatives
) {
    public static RunResponse fromEntity(AnalysisRun run) {
        List<StageResponse> stageResponses = (run.getStages() != null)
                ? run.getStages().stream().map(StageResponse::fromEntity).toList()
                : List.of();

        return new RunResponse(
                run.getId(),
                run.getStatus(),
                run.getCurrentStageId(),
                stageResponses,
                run.getElapsedSeconds(),
                run.isHasAlternatives()
        );
    }
}

