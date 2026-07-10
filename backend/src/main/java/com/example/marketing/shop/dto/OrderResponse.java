package com.example.marketing.shop.dto;

import com.example.marketing.shop.domain.OrderDetails;
import com.example.marketing.shop.domain.Orders;
import com.example.marketing.shop.service.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Public view of an order. Returned instead of the Orders entity so the nested
 * Customer never serializes its password, and the response is a clean, bounded
 * aggregate (customer summary + line items) rather than the full entity graph.
 */
public record OrderResponse(
        String id,
        Integer orderNo,
        LocalDate orderDate,
        BigDecimal orderTotal,
        LocalDate shippingDate,
        String isDelivered,
        OrderStatus orderStatus,
        CustomerResponse customer,
        List<OrderDetailResponse> details
) {
    public static OrderResponse from(Orders order, List<OrderDetails> details) {
        return new OrderResponse(
                order.getId(),
                order.getOrderNo(),
                order.getOrderDate(),
                order.getOrderTotal(),
                order.getShippingDate(),
                order.getIsDelivered(),
                order.getOrderStatus(),
                order.getCustomer() != null ? CustomerResponse.from(order.getCustomer()) : null,
                details.stream().map(OrderDetailResponse::from).toList()
        );
    }
}
