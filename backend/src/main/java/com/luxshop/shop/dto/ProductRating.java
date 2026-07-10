package com.luxshop.shop.dto;

/**
 * Aggregate rating for a product, produced by a grouped review query.
 * average is null / count is 0 for products with no reviews.
 */
public record ProductRating(String productId, Double average, Long count) {
}
