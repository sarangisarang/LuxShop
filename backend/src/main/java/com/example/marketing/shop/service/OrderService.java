package com.example.marketing.shop.service;
import com.example.marketing.shop.domain.Customer;
import com.example.marketing.shop.domain.OrderDetails;
import com.example.marketing.shop.domain.Orders;
import com.example.marketing.shop.domain.Product;
import com.example.marketing.shop.repository.CustomerRepository;
import com.example.marketing.shop.repository.OrderDetailsRepository;
import com.example.marketing.shop.repository.OrdersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class OrderService {
    @Autowired
    private OrderDetailsRepository orderDetailsRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private OrdersRepository ordersRepository;

    // Total value of everything ordered = sum of (unit price * quantity) across all
    // order details. The previous version ignored quantity and threw NPE on details
    // with a null product/price.
    public BigInteger getTotalOrderedAmount() {
        return orderDetailsRepository.findAll().stream()
                .map(OrderService::lineTotal)
                .reduce(BigInteger.ZERO, BigInteger::add);
    }

    private static BigInteger lineTotal(OrderDetails detail) {
        Product product = detail.getProduct();
        Integer qty = detail.getQty();
        if (product == null || product.getPrece() == null || qty == null) {
            return BigInteger.ZERO;
        }
        return product.getPrece().multiply(BigInteger.valueOf(qty));
    }

    public Orders createSaveOrders(@RequestBody Orders orders, String CustomerId) {
        orders.setId(UUID.randomUUID().toString());
        Customer customer = customerRepository.findById(CustomerId).orElseThrow();
        orders.setCustomer(customer);
        return ordersRepository.save(orders);
    }

    // #8: an order can only be edited while it is still Pending. Previously this
    // checked the request body's status (always effectively skipping the guard) and
    // just printed instead of failing, so the caller never learned the update was rejected.
    public Orders createUpdateOrder(@RequestBody Orders orders, String id) {
        Orders ordersToUpdate = ordersRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + id));
        if (ordersToUpdate.getOrderStatus() != OrderStatus.Pending) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only a Pending order can be updated (current status: " + ordersToUpdate.getOrderStatus() + ")");
        }
        ordersToUpdate.setOrderNo(orders.getOrderNo());
        ordersToUpdate.setOrderDate(orders.getOrderDate());
        ordersToUpdate.setOrderTotal(orders.getOrderTotal());
        ordersToUpdate.setShippingDate(orders.getShippingDate());
        ordersToUpdate.setIsDelivered(orders.getIsDelivered());
        return ordersRepository.save(ordersToUpdate);
    }

    // #7: order-status state machine. Key = target status, value = statuses it may
    // legally be reached from. Anything else is rejected instead of silently applied.
    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            OrderStatus.Processing, Set.of(OrderStatus.Pending),
            OrderStatus.shipped, Set.of(OrderStatus.Processing),
            OrderStatus.closed, Set.of(OrderStatus.shipped),
            OrderStatus.Pending, Set.of(OrderStatus.Processing) // allow reverting before shipping
    );

    private Orders changeStatus(String id, OrderStatus target) {
        Orders order = ordersRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + id));
        OrderStatus current = order.getOrderStatus();
        if (current == target) {
            return order; // idempotent no-op
        }
        if (!ALLOWED_TRANSITIONS.getOrDefault(target, Set.of()).contains(current)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Invalid status transition: " + current + " -> " + target);
        }
        order.setOrderStatus(target);
        return ordersRepository.save(order);
    }

    public Orders updateOrderStatusProcess(String id) {
        return changeStatus(id, OrderStatus.Processing);
    }

    public Orders updateOrderStatusSchip(String id) {
        return changeStatus(id, OrderStatus.shipped);
    }

    public Orders updateOrderStatusClose(String id) {
        return changeStatus(id, OrderStatus.closed);
    }

    public Orders updateOrderStatusPending(String id) {
        return changeStatus(id, OrderStatus.Pending);
    }

    @Transactional
    public void  deleteOrderWithDetails(String id) throws Exception {
        Orders orders = ordersRepository.findById(id).orElseThrow();
        if(orders.getOrderStatus()!=OrderStatus.Pending){
            throw new Exception("Not allowed to delete  order");
        }
        Optional<List<OrderDetails>> orderDetails = orderDetailsRepository.findAllByOrders(orders);
        if(!orderDetails.isEmpty()){
            orderDetailsRepository.deleteAll(orderDetails.get());
        }
        ordersRepository.delete(orders);
    }
}