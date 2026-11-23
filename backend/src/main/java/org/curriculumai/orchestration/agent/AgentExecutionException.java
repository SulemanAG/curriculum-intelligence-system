package org.curriculumai.orchestration.agent;

public class AgentExecutionException extends RuntimeException {

    private final boolean retryable;
    private final String errorType;

    public AgentExecutionException(String message, boolean retryable, String errorType) {
        super(message);
        this.retryable = retryable;
        this.errorType = errorType;
    }

    public AgentExecutionException(String message, Throwable cause, boolean retryable, String errorType) {
        super(message, cause);
        this.retryable = retryable;
        this.errorType = errorType;
    }

    public boolean isRetryable() {
        return retryable;
    }

    public String getErrorType() {
        return errorType;
    }
}

