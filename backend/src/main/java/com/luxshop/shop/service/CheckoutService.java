package com.luxshop.shop.service;

import com.luxshop.shop.domain.Customer;
import com.luxshop.shop.domain.OrderDetails;
import com.luxshop.shop.domain.Orders;
import com.luxshop.shop.dto.CheckoutRequest;
import com.luxshop.shop.repository.CustomerRepository;
import com.luxshop.shop.repository.OrdersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Turns a guest checkout into a persisted order: finds or creates the customer,
 * opens a Pending order, then adds each cart line through OrderDetailsService
 * (which derives the price, computes the subtotal, checks/decrements stock and
 * recalculates the order total). The whole thing is one transaction, so a
 * failure on any line (e.g. insufficient stock) rolls the entire order back.
 */
@Service
public class CheckoutService {

    private final CustomerRepository customerRepository;
    private final OrdersRepository ordersRepository;
    private final OrderDetailsService orderDetailsService;

    public CheckoutService(CustomerRepository customerRepository,
                           OrdersRepository ordersRepository,
                           OrderDetailsService orderDetailsService) {
        this.customerRepository = customerRepository;
        this.ordersRepository = ordersRepository;
        this.orderDetailsService = orderDetailsService;
    }

    @Transactional
    public Orders checkout(CheckoutRequest request) {
        Customer customer = customerRepository.findFirstByEmail(request.email())
                .orElseGet(() -> {
                    Customer c = new Customer();
                    c.setId(UUID.randomUUID().toString());
                    c.setEmail(request.email());
                    c.setFirstName(request.firstName());
                    c.setLastName(request.lastName());
                    c.setAddress(request.address());
                    c.setCity(request.city());
                    return customerRepository.save(c);
                });

        Orders order = new Orders();
        order.setId(UUID.randomUUID().toString());
        order.setOrderNo((int) ordersRepository.count() + 1001);
        order.setOrderDate(LocalDate.now());
        order.setOrderStatus(OrderStatus.Pending);
        order.setIsDelivered(false);
        order.setOrderTotal(BigDecimal.ZERO);
        order.setCustomer(customer);
        ordersRepository.save(order);

        for (CheckoutRequest.CheckoutItem item : request.items()) {
            OrderDetails line = new OrderDetails();
            line.setQty(item.qty());
            // Derives price, checks/decrements stock and recalculates the order total.
            orderDetailsService.createOrderDetails(line, order.getId(), item.productId());
        }

        // Reload so the recalculated total is reflected.
        return ordersRepository.findById(order.getId()).orElseThrow();
    }
}
