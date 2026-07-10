package com.luxshop.shop.dto;

import com.luxshop.shop.domain.Product;

import java.math.BigDecimal;

/**
 * Public view of a product. Returned instead of the Product entity: it exposes
 * a clean, stable shape (nested CategoryResponse, no soft-delete flag) and
 * deliberately omits the heavy image byte arrays, which the storefront does
 * not use in listings.
 */
public record ProductResponse(
        String id,
        String productName,
        String productDesc,
        String imageUrl,
        BigDecimal price,
        Integer stock,
        CategoryResponse category
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getProductName(),
                product.getProductDesc(),
                product.getImageUrl(),
                product.getPrice(),
                product.getStock(),
                product.getCategory() != null ? CategoryResponse.from(product.getCategory()) : null
        );
    }
}
