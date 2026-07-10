package com.example.marketing.shop.service;
import com.example.marketing.shop.domain.Customer;
import com.example.marketing.shop.domain.OrderDetails;
import com.example.marketing.shop.domain.Orders;
import com.example.marketing.shop.domain.Product;
import com.example.marketing.shop.repository.CustomerRepository;
import com.example.marketing.shop.repository.OrderDetailsRepository;
import com.example.marketing.shop.repository.OrdersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {
    @Autowired
    private OrderDetailsRepository orderDetailsRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private OrdersRepository ordersRepository;

    public BigInteger getTotalOrderedAmount() {
        BigInteger amount = BigInteger.ZERO;
        List<OrderDetails> details = orderDetailsRepository.findAll();
        List<Product> products = details.stream().map(d -> d.getProduct()).toList();
        for (Product product : products) {
            amount = product.getPrece().add(amount);
        }
        return amount;
    }

    public Orders createSaveOrders(@RequestBody Orders orders, String CustomerId) {
        orders.setId(UUID.randomUUID().toString());
        Customer customer = customerRepository.findById(CustomerId).orElseThrow();
        orders.setCustomer(customer);
        return ordersRepository.save(orders);
    }

    public Orders createUpdateOrder(@RequestBody Orders orders, String id) {
        Orders ordersToUpdate = ordersRepository.findById(id).orElseThrow();
        if(orders.getOrderStatus() != OrderStatus.Pending){
            System.out.println("Not allowed to update  order");
        }else{
            ordersToUpdate.setOrderNo(orders.getOrderNo());
            ordersToUpdate.setOrderDate(orders.getOrderDate());
            ordersToUpdate.setOrderTotal(orders.getOrderTotal());
            ordersToUpdate.setShippingDate(orders.getShippingDate());
            ordersToUpdate.setIsDelivered(orders.getIsDelivered());
        }
        return  ordersRepository.save(ordersToUpdate);
    }

    public Orders updateOrderStatusProcess(String id) {
        Orders ordersToUpdate = ordersRepository.findById(id).orElseThrow();
        ordersToUpdate.setOrderStatus(OrderStatus.Processing);
        return ordersRepository.save(ordersToUpdate);
    }

    public Orders updateOrderStatusSchip(String id) {
        Orders ordersToUpdate = ordersRepository.findById(id).orElseThrow();
        if (ordersToUpdate.getOrderStatus() == OrderStatus.Processing){
            ordersToUpdate.setOrderStatus(OrderStatus.shipped);
            ordersRepository.save(ordersToUpdate);
        } else {
            System.out.println("Not allowed to ship a Pending order");
        }
        return ordersRepository.save(ordersToUpdate);
    }

    public Orders updateOrderStatusClose(String id){
        Orders ordersToUpdate = ordersRepository.findById(id).orElseThrow();
        ordersToUpdate.setOrderStatus(OrderStatus.closed);
        return ordersRepository.save(ordersToUpdate);
    }

    public Orders updateOrderStatusPending(String id){
        Orders ordersToUpdate = ordersRepository.findById(id).orElseThrow();
        ordersToUpdate.setOrderStatus(OrderStatus.Pending);
        return ordersRepository.save(ordersToUpdate);
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