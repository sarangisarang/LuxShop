package com.luxshop.shop.dto;

import jakarta.validation.constraints.NotBlank;

/** Credentials posted to /auth/login. */
public record LoginRequest(
        @NotBlank(message = "{validation.login.username.required}")
        String username,

        @NotBlank(message = "{validation.login.password.required}")
        String password
) {
}
