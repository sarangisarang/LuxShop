package com.luxshop.shop.dto;

import com.luxshop.shop.domain.ProductImage;

/** An extra gallery image with its id (so admins can delete it). */
public record ProductImageResponse(Long id, String url, int position) {
    public static ProductImageResponse from(ProductImage img) {
        return new ProductImageResponse(img.getId(), img.getUrl(), img.getPosition());
    }
}
