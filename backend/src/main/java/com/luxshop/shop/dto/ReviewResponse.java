package com.luxshop.shop.dto;

import com.luxshop.shop.domain.Review;

import java.time.Instant;

/** Public view of a product review. */
public record ReviewResponse(
        Long id,
        String authorName,
        int rating,
        String comment,
        Instant createdAt
) {
    public static ReviewResponse from(Review r) {
        return new ReviewResponse(
                r.getId(),
                r.getAuthorName(),
                r.getRating(),
                r.getComment(),
                r.getCreatedAt()
        );
    }
}
