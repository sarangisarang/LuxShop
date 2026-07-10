package com.luxshop.shop.controller;
import com.luxshop.shop.domain.OrderDetails;
import com.luxshop.shop.domain.Orders;
import com.luxshop.shop.dto.OrderDetailResponse;
import com.luxshop.shop.dto.OrderResponse;
import com.luxshop.shop.repository.OrderDetailsRepository;
import com.luxshop.shop.repository.OrdersRepository;
import jakarta.validation.Valid;
import com.luxshop.shop.service.OrderDetailsService;
import com.luxshop.shop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/shop")
public class OrderController {
    @Autowired
    private OrdersRepository ordersRepository;
    @Autowired
    private OrderDetailsRepository orderDetailsRepository;
    @Autowired
    private OrderDetailsService orderDetailsService;
    @Autowired
    private OrderService orderService;

    // Builds an OrderResponse together with its line items, so no Orders entity
    // (and therefore no nested Customer password) is ever serialized.
    private OrderResponse toOrderResponse(Orders order) {
        List<OrderDetails> details = orderDetailsRepository.findAllByOrders(order).orElse(List.of());
        return OrderResponse.from(order, details);
    }

    @GetMapping("/")
    public String Present(){
        return "Welcome your shop";
    }
    // Orderdetails: GetMapping, PostMapping, PutMapping, DeleteMapping.

    @GetMapping("/orderdetails")
    public List<OrderDetailResponse> getallOrderDetails(){
        return orderDetailsRepository.findAll().stream().map(OrderDetailResponse::from).toList();
    }

    @PostMapping("/orderdetail/{orderId}/{productId}") // this is work! Tested all ok!
    public OrderDetailResponse saveOrderDetails(@Valid @RequestBody OrderDetails orderDetails, @PathVariable String orderId, @PathVariable String productId){
        return OrderDetailResponse.from(orderDetailsService.createOrderDetails(orderDetails,orderId,productId));
    }

    @GetMapping("/orderdetail/{id}")
    public OrderDetailResponse getOrderDetails(@PathVariable String id) {
        return OrderDetailResponse.from(orderDetailsRepository.findById(id).orElseThrow());
    }

    @PutMapping("/orderdetail/{id}") // This is works, tested all ok!
    public OrderDetailResponse updateOrderdetails(@Valid @RequestBody OrderDetails orderDetails, @PathVariable String id){
        return OrderDetailResponse.from(orderDetailsService.UpdeteOrderDetails(orderDetails, id));
    }

    @DeleteMapping("/orderdetails/{id}")
    public void deleteOrderDetails(@PathVariable String id) {
        orderDetailsService.deleteOrderDetails(id);
    }

    // Order: GetMapping, PostMapping, PutMapping, DeleteMapping.

    @GetMapping("/order")
    public List<OrderResponse> getAllOrders(){
        return ordersRepository.findAll().stream().map(this::toOrderResponse).toList();
    }

    @GetMapping("/order/{id}")
    public OrderResponse getOrder(@PathVariable String id) {
        return toOrderResponse(ordersRepository.findById(id).orElseThrow());
    }

    @PostMapping("/order/{CustomerId}") // This is works, all is ok!
    public OrderResponse saveOrders(@RequestBody Orders orders, @PathVariable String CustomerId){
        return toOrderResponse(orderService.createSaveOrders(orders,CustomerId));
    }

    @PutMapping("/order/{id}") // This is works, all is ok!
    public OrderResponse updateOrder(@RequestBody Orders orders, @PathVariable String id){
        return toOrderResponse(orderService.createUpdateOrder(orders,id));
    }

    @PutMapping("/order/{id}/process") // Test this all ok!.
    public OrderResponse updateOrderStatusProcess(@PathVariable String id){
        return toOrderResponse(orderService.updateOrderStatusProcess(id));
    }

    @PutMapping("/order/{id}/ship") // Test this all ok!.
    public OrderResponse updateOrderStatusShip(@PathVariable String id){
        return toOrderResponse(orderService.updateOrderStatusShip(id));
    }

    @PutMapping("/order/{id}/close") // Test this all ok!.
    public OrderResponse updateOrderStatusClose(@PathVariable String id){
        return toOrderResponse(orderService.updateOrderStatusClose(id));
    }

    @PutMapping("/order/{id}/pending") // Test this all ok!.
    public OrderResponse updateOrderStatusPending(@PathVariable String id){
        return toOrderResponse(orderService.updateOrderStatusPending(id));
    }

    @DeleteMapping("/order/{id}") // this is problem!
    public void deleteOrderWithDetails(@PathVariable String id) throws Exception {
      orderService.deleteOrderWithDetails(id);
    }
}
