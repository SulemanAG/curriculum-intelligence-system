package org.curriculumai.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        T data,
        String error,
        String message,
        String timestamp
) {
    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(true, data, null, message, Instant.now().toString());
    }

    public static <T> ApiResponse<T> ok(T data) {
        return ok(data, "Operation completed successfully.");
    }

    public static <T> ApiResponse<T> failure(String error, String message) {
        return new ApiResponse<>(false, null, error, message, Instant.now().toString());
    }

    public static <T> ApiResponse<T> failure(String error) {
        return failure(error, "Request could not be processed.");
    }
}

