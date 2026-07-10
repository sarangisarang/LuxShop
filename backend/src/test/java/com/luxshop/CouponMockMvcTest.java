package com.luxshop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Coupon validation and its effect on checkout. Product 8 (AirPods, 749) x2 =
 * 1498 gross; WELCOME10 (10% off) nets 1348.20. Rolled back per test.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CouponMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    private String cart(String coupon) {
        String base = "{\"firstName\":\"A\",\"lastName\":\"B\",\"email\":\"c@d.com\","
                + "\"address\":\"a\",\"city\":\"c\",\"items\":[{\"productId\":\"8\",\"qty\":2}]";
        return coupon == null ? base + "}" : base + ",\"couponCode\":\"" + coupon + "\"}";
    }

    @Test
    void validCoupon_isCaseInsensitive() throws Exception {
        mockMvc.perform(get("/shop/coupon/welcome10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("WELCOME10"))
                .andExpect(jsonPath("$.percentOff").value(10));
    }

    @Test
    void inactiveCoupon_is404() throws Exception {
        mockMvc.perform(get("/shop/coupon/EXPIRED")).andExpect(status().isNotFound());
    }

    @Test
    void unknownCoupon_is404() throws Exception {
        mockMvc.perform(get("/shop/coupon/NOPE")).andExpect(status().isNotFound());
    }

    @Test
    void checkoutWithCoupon_appliesDiscount() throws Exception {
        mockMvc.perform(post("/shop/checkout").contentType(MediaType.APPLICATION_JSON).content(cart("WELCOME10")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderTotal").value(1348.20));
    }

    @Test
    void checkoutWithInvalidCoupon_keepsFullPrice() throws Exception {
        mockMvc.perform(post("/shop/checkout").contentType(MediaType.APPLICATION_JSON).content(cart("EXPIRED")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderTotal").value(1498.00));
    }
}
