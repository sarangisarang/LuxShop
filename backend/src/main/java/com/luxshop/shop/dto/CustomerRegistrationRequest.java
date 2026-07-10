package com.luxshop.shop.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Incoming payload for self-service customer registration.
 * A DTO is used instead of the ServiceUser/Customer entities so the client
 * cannot set fields like id, roles or an already-hashed password directly.
 */
public record CustomerRegistrationRequest(
        @NotBlank(message = "{validation.registration.email.required}")
        @Email(message = "{validation.registration.email.invalid}")
        String email,

        @NotBlank(message = "{validation.registration.password.required}")
        @Size(min = 8, message = "{validation.registration.password.size}")
        String password,

        @NotBlank(message = "{validation.registration.firstName.required}")
        String firstName,

        @NotBlank(message = "{validation.registration.lastName.required}")
        String lastName,

        String address,
        String city
) {
}
