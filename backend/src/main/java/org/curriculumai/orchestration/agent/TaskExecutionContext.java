package org.curriculumai.orchestration.agent;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public record TaskExecutionContext(
        String runId,
        String taskId,
        String taskKey,
        String subject,
        String rawPrompt,
        Map<String, Object> academicScope,
        Map<String, Object> inputParameters,
        Map<String, StructuredAgentResult> priorTaskOutputs,
        List<String> evidenceReferences,
        String userId
) {
    public TaskExecutionContext {
        if (academicScope == null) academicScope = Collections.emptyMap();
        if (inputParameters == null) inputParameters = Collections.emptyMap();
        if (priorTaskOutputs == null) priorTaskOutputs = Collections.emptyMap();
        if (evidenceReferences == null) evidenceReferences = Collections.emptyList();
    }
}

