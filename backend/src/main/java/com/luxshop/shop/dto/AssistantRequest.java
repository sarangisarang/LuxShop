package com.luxshop.shop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** A shopper's question for the AI assistant. */
public record AssistantRequest(
        @NotBlank(message = "Message is required")
        @Size(max = 500, message = "Message must be at most {max} characters")
        String message
) {
}
