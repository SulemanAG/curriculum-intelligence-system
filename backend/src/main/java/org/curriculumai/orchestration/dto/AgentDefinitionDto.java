package org.curriculumai.orchestration.dto;

import org.curriculumai.orchestration.agent.AgentDefinition;
import org.curriculumai.orchestration.domain.AgentStatus;

import java.util.List;

public record AgentDefinitionDto(
        String agentId,
        String displayName,
        String roleDescription,
        AgentStatus status,
        List<String> toolsPermitted,
        String inputSchemaJson,
        String outputSchemaJson
) {
    public static AgentDefinitionDto fromRecord(AgentDefinition def) {
        return new AgentDefinitionDto(
                def.id(),
                def.displayName(),
                def.description(),
                def.status(),
                def.permittedTools(),
                def.inputSchemaJson(),
                def.outputSchemaJson()
        );
    }
}
