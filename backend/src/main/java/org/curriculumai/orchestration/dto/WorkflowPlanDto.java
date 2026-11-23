package org.curriculumai.orchestration.dto;

import org.curriculumai.orchestration.domain.WorkflowType;

import java.util.List;

public record WorkflowPlanDto(
        String runId,
        WorkflowType workflowType,
        List<WorkflowTaskDto> tasks,
        List<TaskDependencyDto> dependencies
) {
}

