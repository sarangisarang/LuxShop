package com.luxshop.shop.repository;
import com.luxshop.shop.domain.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdersRepository extends JpaRepository<Orders,String>{
    // A customer's order history, newest first (used by guest "My Orders").
    List<Orders> findByCustomer_EmailOrderByOrderDateDesc(String email);
}
