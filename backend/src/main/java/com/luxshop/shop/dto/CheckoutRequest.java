package com.luxshop.shop.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

/** Guest checkout payload: customer details plus the cart items to order. */
public record CheckoutRequest(
        @NotBlank(message = "{validation.registration.firstName.required}")
        String firstName,

        @NotBlank(message = "{validation.registration.lastName.required}")
        String lastName,

        @NotBlank(message = "{validation.registration.email.required}")
        @Email(message = "{validation.registration.email.invalid}")
        String email,

        @NotBlank(message = "Address is required")
        String address,

        @NotBlank(message = "City is required")
        String city,

        @NotEmpty(message = "Cart is empty")
        @Valid
        List<CheckoutItem> items
) {
    public record CheckoutItem(
            @NotBlank(message = "Product id is required")
            String productId,

            @NotNull(message = "Qty is required")
            @Positive(message = "Qty must be a positive number")
            Integer qty
    ) {
    }
}
