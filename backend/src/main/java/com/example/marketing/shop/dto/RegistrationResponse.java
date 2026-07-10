package com.example.marketing.shop.dto;

/** Response returned after a successful registration (never exposes the password). */
public record RegistrationResponse(
        String id,
        String username,
        String message
) {
}
