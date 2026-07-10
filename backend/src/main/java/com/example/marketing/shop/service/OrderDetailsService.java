package com.example.marketing.shop.service;
import com.example.marketing.shop.domain.OrderDetails;
import com.example.marketing.shop.domain.Orders;
import com.example.marketing.shop.domain.Product;
import com.example.marketing.shop.repository.OrderDetailsRepository;
import com.example.marketing.shop.repository.OrdersRepository;
import com.example.marketing.shop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class OrderDetailsService {
    @Autowired
    private OrderDetailsRepository orderDetailsRepository;
    @Autowired
    private OrdersRepository ordersRepository;
    @Autowired
    private ProductRepository productRepository;

    /**
     * Adds a line item to an order. Saving the line and decrementing the product's
     * stock happen in one transaction, so a failure never leaves stock reduced
     * without a matching order detail (or vice versa).
     *
     * The unit price and subtotal are derived from the product, not trusted from
     * the client: Subtotal = productPrice * Qty.
     */
    @Transactional
    public OrderDetails createOrderDetails(OrderDetails orderDetails, String orderId, String productId) {
        Orders orders = ordersRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + orderId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + productId));

        Integer qty = orderDetails.getQty();
        if (qty == null || qty <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Qty must be a positive number");
        }

        BigDecimal unitPrice = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
        int available = product.getStock() != null ? product.getStock() : 0;

        if (available < qty) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Insufficient stock for product " + productId + ": " + available + " available, " + qty + " requested");
        }

        orderDetails.setId(UUID.randomUUID().toString());
        orderDetails.setOrders(orders);
        orderDetails.setProduct(product);
        orderDetails.setPrice(unitPrice);
        orderDetails.setSubtotal(unitPrice.multiply(BigDecimal.valueOf(qty)));

        // Reserve the stock; rolls back with the line-item save if anything fails.
        product.setStock(available - qty);
        productRepository.save(product);

        OrderDetails saved = orderDetailsRepository.save(orderDetails);
        recalculateOrderTotal(orders);
        return saved;
    }

    @Transactional
    public void deleteOrderDetails(String id){
        OrderDetails orderDetailsdelete = orderDetailsRepository.findById(id).orElseThrow();
        if (orderDetailsdelete.getOrders().getOrderStatus() == OrderStatus.Pending) {
            Orders orders = orderDetailsdelete.getOrders();
            orderDetailsRepository.delete(orderDetailsdelete);
            recalculateOrderTotal(orders);
        } else {
            System.out.println("Not allowed to ship a Pending order");
        }
    }

    @Transactional
    public OrderDetails UpdeteOrderDetails(OrderDetails orderDetails, Orders orders, String id) {
        OrderDetails orderDetailsToUpdate = orderDetailsRepository.findById(id).orElseThrow();
        Orders ordersstatus = ordersRepository.findById(orders.getId()).orElseThrow();
        if (ordersstatus.getOrderStatus() == OrderStatus.Pending) {
            orderDetailsToUpdate.setQty(orderDetails.getQty());
            orderDetailsToUpdate.setPrice(orderDetails.getPrice());
            orderDetailsToUpdate.setSubtotal(orderDetails.getSubtotal());
            orderDetailsRepository.save(orderDetailsToUpdate);
            recalculateOrderTotal(ordersstatus);
        } else {
            System.out.println("update/delete OrderDetails only from Pending Order");
        }
        return orderDetailsRepository.save(orderDetailsToUpdate);
    }

    /**
     * Recomputes and persists an order's total as the sum of its line-item
     * subtotals. Called whenever the order's details change so orderTotal stays
     * consistent (Project #5: #9).
     */
    private void recalculateOrderTotal(Orders orders) {
        List<OrderDetails> lines = orderDetailsRepository.findAllByOrders(orders).orElse(List.of());
        BigDecimal total = lines.stream()
                .map(line -> line.getSubtotal() != null ? line.getSubtotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        orders.setOrderTotal(total);
        ordersRepository.save(orders);
    }
}
