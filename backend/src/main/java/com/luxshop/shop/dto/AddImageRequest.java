package com.luxshop.shop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Admin payload to add a gallery image to a product. */
public record AddImageRequest(
        @NotBlank(message = "Image URL is required")
        @Size(max = 1000, message = "URL must be at most {max} characters")
        String url
) {
}
