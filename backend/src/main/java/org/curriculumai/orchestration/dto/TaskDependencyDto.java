package org.curriculumai.orchestration.dto;

import org.curriculumai.orchestration.domain.TaskDependency;

public record TaskDependencyDto(
        String id,
        String taskId,
        String dependsOnTaskId,
        boolean required
) {
    public static TaskDependencyDto fromEntity(TaskDependency dep) {
        return new TaskDependencyDto(
                dep.getId(),
                dep.getTaskId(),
                dep.getDependsOnTaskId(),
                dep.isRequired()
        );
    }
}
