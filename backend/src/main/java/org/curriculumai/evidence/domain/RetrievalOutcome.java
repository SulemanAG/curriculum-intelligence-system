package org.curriculumai.evidence.domain;

public enum RetrievalOutcome {
    PENDING,
    FETCHING,
    RETRIEVED,
    FAILED,
    ACCESS_DENIED,
    UNSUPPORTED_FORMAT,
    SSRF_BLOCKED,
    TIMEOUT
}

