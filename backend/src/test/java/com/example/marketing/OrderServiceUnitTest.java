package com.example.marketing;

import com.example.marketing.shop.domain.OrderDetails;
import com.example.marketing.shop.domain.Orders;
import com.example.marketing.shop.domain.Product;
import com.example.marketing.shop.repository.CustomerRepository;
import com.example.marketing.shop.repository.OrderDetailsRepository;
import com.example.marketing.shop.repository.OrdersRepository;
import com.example.marketing.shop.service.OrderService;
import com.example.marketing.shop.service.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Fast, DB-free unit tests for the two most critical pieces of order logic:
 * the status state machine (#7/#8) and the price*quantity total calculation.
 * These guard against silent regressions that the integration tests do not cover.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceUnitTest {

    @Mock
    private OrdersRepository ordersRepository;
    @Mock
    private OrderDetailsRepository orderDetailsRepository;
    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private OrderService orderService;

    private Orders orderWithStatus(OrderStatus status) {
        Orders order = new Orders();
        order.setId("o1");
        order.setOrderStatus(status);
        return order;
    }

    private OrderDetails detail(BigInteger unitPrice, Integer qty) {
        Product product = new Product();
        product.setPrice(unitPrice);
        OrderDetails d = new OrderDetails();
        d.setProduct(product);
        d.setQty(qty);
        return d;
    }

    // --- State machine: allowed transitions -------------------------------

    @Test
    void process_movesPendingToProcessing() {
        when(ordersRepository.findById("o1")).thenReturn(Optional.of(orderWithStatus(OrderStatus.Pending)));
        when(ordersRepository.save(any(Orders.class))).thenAnswer(i -> i.getArgument(0));

        Orders result = orderService.updateOrderStatusProcess("o1");

        assertEquals(OrderStatus.Processing, result.getOrderStatus());
        verify(ordersRepository).save(any(Orders.class));
    }

    @Test
    void ship_movesProcessingToShipped() {
        when(ordersRepository.findById("o1")).thenReturn(Optional.of(orderWithStatus(OrderStatus.Processing)));
        when(ordersRepository.save(any(Orders.class))).thenAnswer(i -> i.getArgument(0));

        Orders result = orderService.updateOrderStatusShip("o1");

        assertEquals(OrderStatus.shipped, result.getOrderStatus());
    }

    @Test
    void close_movesShippedToClosed() {
        when(ordersRepository.findById("o1")).thenReturn(Optional.of(orderWithStatus(OrderStatus.shipped)));
        when(ordersRepository.save(any(Orders.class))).thenAnswer(i -> i.getArgument(0));

        Orders result = orderService.updateOrderStatusClose("o1");

        assertEquals(OrderStatus.closed, result.getOrderStatus());
    }

    @Test
    void pending_canRevertFromProcessing() {
        when(ordersRepository.findById("o1")).thenReturn(Optional.of(orderWithStatus(OrderStatus.Processing)));
        when(ordersRepository.save(any(Orders.class))).thenAnswer(i -> i.getArgument(0));

        Orders result = orderService.updateOrderStatusPending("o1");

        assertEquals(OrderStatus.Pending, result.getOrderStatus());
    }

    // --- State machine: rejected transitions ------------------------------

    @Test
    void ship_fromPending_isRejected() {
        when(ordersRepository.findById("o1")).thenReturn(Optional.of(orderWithStatus(OrderStatus.Pending)));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> orderService.updateOrderStatusShip("o1"));

        assertEquals(409, ex.getStatusCode().value());
        verify(ordersRepository, never()).save(any());
    }

    @Test
    void pending_fromClosed_isRejected() {
        when(ordersRepository.findById("o1")).thenReturn(Optional.of(orderWithStatus(OrderStatus.closed)));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> orderService.updateOrderStatusPending("o1"));

        assertEquals(409, ex.getStatusCode().value());
        verify(ordersRepository, never()).save(any());
    }

    @Test
    void sameStatus_isIdempotentNoOp() {
        when(ordersRepository.findById("o1")).thenReturn(Optional.of(orderWithStatus(OrderStatus.Processing)));

        Orders result = orderService.updateOrderStatusProcess("o1");

        assertEquals(OrderStatus.Processing, result.getOrderStatus());
        verify(ordersRepository, never()).save(any());
    }

    @Test
    void changeStatus_onMissingOrder_is404() {
        when(ordersRepository.findById("missing")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> orderService.updateOrderStatusProcess("missing"));

        assertEquals(404, ex.getStatusCode().value());
    }

    // --- Total calculation ------------------------------------------------

    @Test
    void total_sumsPriceTimesQuantityAcrossDetails() {
        // 1 x 3000 + 2 x 50 = 3100
        when(orderDetailsRepository.findAll()).thenReturn(List.of(
                detail(BigInteger.valueOf(3000), 1),
                detail(BigInteger.valueOf(50), 2)
        ));

        assertEquals(BigInteger.valueOf(3100), orderService.getTotalOrderedAmount());
    }

    @Test
    void total_ofNoDetails_isZero() {
        when(orderDetailsRepository.findAll()).thenReturn(List.of());

        assertEquals(BigInteger.ZERO, orderService.getTotalOrderedAmount());
    }

    @Test
    void total_ignoresDetailsWithNullProductPriceOrQty_withoutNpe() {
        OrderDetails nullProduct = new OrderDetails();
        nullProduct.setQty(5);
        OrderDetails nullQty = detail(BigInteger.valueOf(100), null);
        when(orderDetailsRepository.findAll()).thenReturn(List.of(
                detail(BigInteger.valueOf(3000), 1),
                nullProduct,
                nullQty
        ));

        assertEquals(BigInteger.valueOf(3000), orderService.getTotalOrderedAmount());
    }

    // --- Update guard (#8): only a Pending order may be edited ------------

    @Test
    void update_allowedWhenPending() {
        when(ordersRepository.findById("o1")).thenReturn(Optional.of(orderWithStatus(OrderStatus.Pending)));
        when(ordersRepository.save(any(Orders.class))).thenAnswer(i -> i.getArgument(0));

        Orders body = new Orders();
        body.setOrderNo(42);
        Orders result = orderService.createUpdateOrder(body, "o1");

        assertEquals(42, result.getOrderNo());
    }

    @Test
    void update_rejectedWhenNotPending() {
        when(ordersRepository.findById("o1")).thenReturn(Optional.of(orderWithStatus(OrderStatus.Processing)));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> orderService.createUpdateOrder(new Orders(), "o1"));

        assertEquals(409, ex.getStatusCode().value());
        verify(ordersRepository, never()).save(any());
    }
}
