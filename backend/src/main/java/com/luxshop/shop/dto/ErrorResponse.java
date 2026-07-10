package com.luxshop.shop.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

/** Consistent error body returned by the global exception handler. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String timestamp,
        int status,
        String error,
        String message,
        String path,
        // Per-field messages for validation failures; omitted for other errors.
        Map<String, String> fields
) {
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(Instant.now().toString(), status, error, message, path, null);
    }

    public static ErrorResponse of(int status, String error, String message, String path, Map<String, String> fields) {
        return new ErrorResponse(Instant.now().toString(), status, error, message, path, fields);
    }
}
