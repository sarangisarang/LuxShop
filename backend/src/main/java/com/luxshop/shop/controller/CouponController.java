package com.luxshop.shop.controller;

import com.luxshop.shop.dto.CouponResponse;
import com.luxshop.shop.repository.CouponRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Public coupon lookup so the cart can validate a code before checkout. */
@RestController
@RequestMapping("/shop/coupon")
public class CouponController {

    private final CouponRepository couponRepository;

    public CouponController(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @GetMapping("/{code}")
    public CouponResponse validate(@PathVariable String code) {
        return couponRepository.findByCodeIgnoreCaseAndActiveTrue(code)
                .map(CouponResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid or expired coupon"));
    }
}
