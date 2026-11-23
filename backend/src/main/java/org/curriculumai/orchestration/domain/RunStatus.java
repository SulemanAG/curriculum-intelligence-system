package org.curriculumai.orchestration.domain;

public enum RunStatus {
    CREATED,
    PLANNED,
    RUNNING,
    WAITING_FOR_INPUT,
    COMPLETED,
    FAILED,
    CANCELLED
}

