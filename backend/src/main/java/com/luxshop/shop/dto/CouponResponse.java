package com.luxshop.shop.dto;

import com.luxshop.shop.domain.Coupon;

/** Public view of a valid coupon. */
public record CouponResponse(String code, int percentOff) {
    public static CouponResponse from(Coupon c) {
        return new CouponResponse(c.getCode().toUpperCase(), c.getPercentOff());
    }
}
