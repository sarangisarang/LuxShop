package com.luxshop.shop.dto;

import jakarta.validation.constraints.NotBlank;

/** Credentials posted to /auth/login. */
public record LoginRequest(
        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Password is required")
        String password
) {
}
