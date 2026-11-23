package org.curriculumai.orchestration.tools;

import java.util.Collections;
import java.util.Map;

public record ToolExecutionContext(
        String runId,
        String taskId,
        String agentId,
        Map<String, Object> parameters
) {
    public ToolExecutionContext {
        if (parameters == null) parameters = Collections.emptyMap();
    }
}

