package com.example.marketing.shop.dto;

import java.time.Instant;

/** Consistent error body returned by the global exception handler. */
public record ErrorResponse(
        String timestamp,
        int status,
        String error,
        String message,
        String path
) {
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(Instant.now().toString(), status, error, message, path);
    }
}
