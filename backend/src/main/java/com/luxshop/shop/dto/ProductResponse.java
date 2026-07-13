package com.luxshop.shop.dto;

import com.luxshop.shop.domain.Product;
import com.luxshop.shop.domain.ProductImage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
        CategoryResponse category,
        Double averageRating,
        int reviewCount,
        List<String> images
) {
    public static ProductResponse from(Product product) {
        return from(product, null);
    }

    /**
     * Enriched with an aggregate rating (pass null for products without reviews).
     * The gallery is left empty here to avoid a lazy-load per product in listings;
     * use {@link #detail} on the single-product endpoint.
     */
    public static ProductResponse from(Product product, ProductRating rating) {
        return build(product, rating, List.of());
    }

    /** Full detail view including the ordered image gallery (main image first). */
    public static ProductResponse detail(Product product, ProductRating rating) {
        List<String> gallery = new ArrayList<>();
        if (product.getImageUrl() != null) {
            gallery.add(product.getImageUrl());
        }
        for (ProductImage img : product.getImages()) {
            gallery.add(img.getUrl());
        }
        return build(product, rating, gallery);
    }

    private static ProductResponse build(Product product, ProductRating rating, List<String> images) {
        boolean rated = rating != null && rating.count() != null && rating.count() > 0;
        // Round the average to one decimal for a stable, display-friendly value.
        Double avg = rated ? Math.round(rating.average() * 10.0) / 10.0 : null;
        int count = rated ? rating.count().intValue() : 0;
        return new ProductResponse(
                product.getId(),
                // Localized to the request locale (Accept-Language), falling back to the base text.
                product.getLocalizedName(),
                product.getLocalizedDescription(),
                product.getImageUrl(),
                product.getPrice(),
                product.getStock(),
                product.getCategory() != null ? CategoryResponse.from(product.getCategory()) : null,
                avg,
                count,
                images
        );
    }
}
