package org.curriculumai.academic.domain;

public enum CurriculumLifecycleStatus {
    DRAFT,
    UNDER_REVIEW,
    APPROVED,
    ARCHIVED;

    public boolean isImmutable() {
        return this == APPROVED || this == ARCHIVED;
    }

    public boolean canTransitionTo(CurriculumLifecycleStatus next) {
        if (this == next) return false;
        return switch (this) {
            case DRAFT -> next == UNDER_REVIEW;
            case UNDER_REVIEW -> next == DRAFT || next == APPROVED;
            case APPROVED -> next == ARCHIVED;
            case ARCHIVED -> false;
        };
    }
}

