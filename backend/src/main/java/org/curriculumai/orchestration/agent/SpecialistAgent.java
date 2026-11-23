package org.curriculumai.orchestration.agent;

import org.curriculumai.orchestration.domain.AgentStatus;

public interface SpecialistAgent {

    String getAgentId();

    String getDisplayName();

    String getDescription();

    AgentStatus getStatus();

    StructuredAgentResult execute(TaskExecutionContext context) throws AgentExecutionException;
}

