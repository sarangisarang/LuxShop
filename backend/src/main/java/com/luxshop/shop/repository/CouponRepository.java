package com.luxshop.shop.repository;

import com.luxshop.shop.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    // Only matches an active code; lookup is case-insensitive.
    Optional<Coupon> findByCodeIgnoreCaseAndActiveTrue(String code);
}
