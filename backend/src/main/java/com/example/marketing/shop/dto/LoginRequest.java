package com.example.marketing.shop.dto;

/** Credentials posted to /auth/login. */
public record LoginRequest(
        String username,
        String password
) {
}
