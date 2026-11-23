package org.curriculumai.orchestration.domain;

public enum WorkflowEventType {
    RUN_CREATED,
    RUN_PLANNED,
    RUN_STARTED,
    TASK_SCHEDULED,
    TASK_STARTED,
    TASK_COMPLETED,
    TASK_FAILED,
    TASK_BLOCKED,
    TASK_RETRYING,
    RUN_COMPLETED,
    RUN_FAILED,
    RUN_CANCELLED,
    RUN_RESUMED,
    TOOL_INVOKED
}

