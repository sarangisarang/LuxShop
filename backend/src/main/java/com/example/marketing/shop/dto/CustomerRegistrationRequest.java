package com.example.marketing.shop.dto;

/**
 * Incoming payload for self-service customer registration.
 * A DTO is used instead of the ServiceUser/Customer entities so the client
 * cannot set fields like id, roles or an already-hashed password directly.
 */
public record CustomerRegistrationRequest(
        String email,
        String password,
        String firstName,
        String lastName,
        String address,
        String city
) {
}
