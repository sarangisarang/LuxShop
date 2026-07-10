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
 * HTTP-level tests for guest checkout: a valid cart persists a Pending order,
 * insufficient stock is rejected, and an empty cart fails validation. Rolled
 * back per test so the shared seed is untouched.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CheckoutMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void validCheckout_creates201OrderWithoutPassword() throws Exception {
        // Product 8 (AirPods) has plenty of stock in the seed.
        String body = "{\"firstName\":\"Beka\",\"lastName\":\"Test\",\"email\":\"guest@example.com\","
                + "\"address\":\"1 Rustaveli\",\"city\":\"Tbilisi\","
                + "\"items\":[{\"productId\":\"8\",\"qty\":2}]}";
        mockMvc.perform(post("/shop/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNo").exists())
                .andExpect(jsonPath("$.orderStatus").value("Pending"))
                .andExpect(jsonPath("$.customer.email").value("guest@example.com"))
                .andExpect(jsonPath("$.customer.password").doesNotExist())
                .andExpect(jsonPath("$.details[0].qty").value(2));
    }

    @Test
    void insufficientStock_returns409() throws Exception {
        String body = "{\"firstName\":\"A\",\"lastName\":\"B\",\"email\":\"a@b.com\","
                + "\"address\":\"a\",\"city\":\"c\","
                + "\"items\":[{\"productId\":\"1\",\"qty\":9999}]}";
        mockMvc.perform(post("/shop/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void orderHistory_byEmail_returnsPlacedOrder() throws Exception {
        String email = "history@example.com";
        String body = "{\"firstName\":\"H\",\"lastName\":\"I\",\"email\":\"" + email + "\","
                + "\"address\":\"a\",\"city\":\"c\","
                + "\"items\":[{\"productId\":\"8\",\"qty\":1}]}";
        mockMvc.perform(post("/shop/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/shop/orders").param("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customer.email").value(email))
                .andExpect(jsonPath("$[0].orderStatus").value("Pending"))
                .andExpect(jsonPath("$[0].customer.password").doesNotExist());
    }

    @Test
    void emptyCart_returns400() throws Exception {
        String body = "{\"firstName\":\"A\",\"lastName\":\"B\",\"email\":\"a@b.com\","
                + "\"address\":\"a\",\"city\":\"c\",\"items\":[]}";
        mockMvc.perform(post("/shop/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.items").exists());
    }
}
