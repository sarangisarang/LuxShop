package com.luxshop.shop.repository;

import com.luxshop.shop.domain.Review;
import com.luxshop.shop.dto.ProductRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Newest first, for a product's review list.
    List<Review> findByProduct_IdOrderByCreatedAtDesc(String productId);

    // One row per product that has reviews: its average rating and review count.
    @Query("select new com.luxshop.shop.dto.ProductRating(r.product.id, avg(r.rating), count(r)) "
            + "from Review r group by r.product.id")
    List<ProductRating> findRatingAggregates();

    // Aggregate rating for a single product (empty when it has no reviews).
    @Query("select new com.luxshop.shop.dto.ProductRating(r.product.id, avg(r.rating), count(r)) "
            + "from Review r where r.product.id = :id group by r.product.id")
    Optional<ProductRating> findRatingByProductId(String id);
}
