package com.example.marketing.shop.dto;

import com.example.marketing.shop.domain.OrderDetails;

import java.math.BigDecimal;

/**
 * Public view of an order line item. Flattens the product to its name and the
 * order to its id, so returning it never drags along the full Product/Orders
 * (and, through Orders, the Customer password) entity graph.
 */
public record OrderDetailResponse(
        String id,
        String productName,
        Integer qty,
        BigDecimal price,
        BigDecimal subtotal,
        String orderId
) {
    public static OrderDetailResponse from(OrderDetails detail) {
        return new OrderDetailResponse(
                detail.getId(),
                detail.getProduct() != null ? detail.getProduct().getProductName() : null,
                detail.getQty(),
                detail.getPrice(),
                detail.getSubtotal(),
                detail.getOrders() != null ? detail.getOrders().getId() : null
        );
    }
}
