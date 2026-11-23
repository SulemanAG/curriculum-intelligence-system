package org.curriculumai.coordinator.dto;

import org.curriculumai.coordinator.ParallelAgentExecution;

public record ParallelAgentResponse(
        String name,
        String role,
        String status,
        String message
) {
    public static ParallelAgentResponse fromEntity(ParallelAgentExecution pa) {
        return new ParallelAgentResponse(
                pa.getAgentName(),
                pa.getRole(),
                pa.getStatus(),
                pa.getMessage()
        );
    }
}

