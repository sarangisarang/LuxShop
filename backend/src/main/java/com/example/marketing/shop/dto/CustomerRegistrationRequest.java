package com.example.marketing.shop.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Incoming payload for self-service customer registration.
 * A DTO is used instead of the ServiceUser/Customer entities so the client
 * cannot set fields like id, roles or an already-hashed password directly.
 */
public record CustomerRegistrationRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password,

        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        String address,
        String city
) {
}
