package com.example.marketing.shop.dto;

import com.example.marketing.shop.domain.Category;

/**
 * Public view of a category. Returned instead of the Category entity so the
 * API contract is decoupled from the persistence model (e.g. the soft-delete
 * flag never leaks).
 */
public record CategoryResponse(
        String id,
        String name,
        String description,
        String image
) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getImage()
        );
    }
}
