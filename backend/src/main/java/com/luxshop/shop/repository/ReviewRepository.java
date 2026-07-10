package com.luxshop.shop.repository;

import com.luxshop.shop.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Newest first, for a product's review list.
    List<Review> findByProduct_IdOrderByCreatedAtDesc(String productId);
}
