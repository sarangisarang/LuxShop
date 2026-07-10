package com.example.marketing.shop.dto;

/** Issued token returned from /auth/login. */
public record AuthResponse(
        String token,
        String tokenType,
        String username,
        long expiresInMs
) {
}
