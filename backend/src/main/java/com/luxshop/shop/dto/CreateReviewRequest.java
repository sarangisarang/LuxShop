package com.luxshop.shop.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Guest-submitted review payload. Validated before it reaches the service. */
public record CreateReviewRequest(
        @NotBlank(message = "{validation.review.author.required}")
        @Size(max = 120, message = "{validation.review.author.size}")
        String authorName,

        @Min(value = 1, message = "{validation.review.rating.range}")
        @Max(value = 5, message = "{validation.review.rating.range}")
        int rating,

        @Size(max = 2000, message = "{validation.review.comment.size}")
        String comment
) {
}
