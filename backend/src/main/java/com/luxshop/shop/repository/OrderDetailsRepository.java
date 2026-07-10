package com.luxshop.shop.repository;
import com.luxshop.shop.domain.Category;
import com.luxshop.shop.domain.OrderDetails;
import com.luxshop.shop.domain.Orders;
import com.luxshop.shop.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderDetailsRepository extends JpaRepository<OrderDetails,String>{
    List<OrderDetails> findAllByProductCategory(Category category);

    Optional<List<OrderDetails>> findAllByOrders(Orders orders);

    Optional<List<OrderDetails>> findAllByProduct(Product product);
}
