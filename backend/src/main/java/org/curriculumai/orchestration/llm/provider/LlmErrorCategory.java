package org.curriculumai.orchestration.llm.provider;

public enum LlmErrorCategory {
    RATE_LIMIT_429,
    SERVER_ERROR_5XX,
    TIMEOUT,
    NETWORK_ERROR,
    AUTH_ERROR_401,
    AUTH_ERROR_403,
    INVALID_REQUEST_400,
    UNCONFIGURED,
    UNKNOWN;

    public boolean isFailoverEligible() {
        return switch (this) {
            case RATE_LIMIT_429, SERVER_ERROR_5XX, TIMEOUT, NETWORK_ERROR, UNCONFIGURED, AUTH_ERROR_401, AUTH_ERROR_403 -> true;
            case INVALID_REQUEST_400, UNKNOWN -> false;
        };
    }
}

