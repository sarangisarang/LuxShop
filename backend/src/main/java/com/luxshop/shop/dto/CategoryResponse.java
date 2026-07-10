package com.luxshop.shop.dto;

import com.luxshop.shop.domain.Category;

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
                // Localized to the request locale (Accept-Language), falling back to the base text.
                category.getLocalizedName(),
                category.getLocalizedDescription(),
                category.getImage()
        );
    }
}
