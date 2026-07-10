package com.luxshop.shop.controller;

import com.luxshop.shop.domain.OrderDetails;
import com.luxshop.shop.domain.Orders;
import com.luxshop.shop.dto.CheckoutRequest;
import com.luxshop.shop.dto.OrderResponse;
import com.luxshop.shop.repository.OrderDetailsRepository;
import com.luxshop.shop.service.CheckoutService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/shop")
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final OrderDetailsRepository orderDetailsRepository;

    public CheckoutController(CheckoutService checkoutService, OrderDetailsRepository orderDetailsRepository) {
        this.checkoutService = checkoutService;
        this.orderDetailsRepository = orderDetailsRepository;
    }

    /** Guest checkout: persists a real order (and its line items) and returns it. */
    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse checkout(@Valid @RequestBody CheckoutRequest request) {
        Orders order = checkoutService.checkout(request);
        List<OrderDetails> details = orderDetailsRepository.findAllByOrders(order).orElse(List.of());
        return OrderResponse.from(order, details);
    }
}
