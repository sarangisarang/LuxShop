package com.example.marketing.shop.exception;

/** Thrown when an operation conflicts with the current state (mapped to HTTP 409). */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
