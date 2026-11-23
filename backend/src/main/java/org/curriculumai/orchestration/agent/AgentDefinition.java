package org.curriculumai.orchestration.agent;

import org.curriculumai.orchestration.domain.AgentStatus;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public record AgentDefinition(
        String id,
        String displayName,
        String description,
        String responsibility,
        String inputSchemaJson,
        String outputSchemaJson,
        List<String> permittedTools,
        AgentStatus status,
        boolean enabled,
        Map<String, Object> executionConfig
) {
    public AgentDefinition {
        if (permittedTools == null) permittedTools = Collections.emptyList();
        if (executionConfig == null) executionConfig = Collections.emptyMap();
    }
}

