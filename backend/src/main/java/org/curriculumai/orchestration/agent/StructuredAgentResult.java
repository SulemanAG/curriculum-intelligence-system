package org.curriculumai.orchestration.agent;

import java.util.List;
import java.util.Map;

public record StructuredAgentResult(
        String runId,
        String taskId,
        String agentId,
        String executionStatus,
        Map<String, Object> findings,
        List<String> evidenceReferences,
        List<String> limitations,
        String errorDetails,
        Map<String, Object> executionMetadata
) {
    public static StructuredAgentResult unImplemented(String runId, String taskId, String agentId, String reason) {
        return new StructuredAgentResult(
                runId,
                taskId,
                agentId,
                "BLOCKED",
                Map.of(),
                List.of(),
                List.of("Specialist agent is registered but not implemented in Phase 3.1"),
                reason,
                Map.of("implementationStatus", "REGISTERED_NOT_IMPLEMENTED")
        );
    }
}

