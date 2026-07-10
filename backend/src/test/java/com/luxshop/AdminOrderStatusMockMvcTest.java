package com.luxshop;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HTTP-level tests for the admin order state machine backing the /admin panel:
 * a guest order starts Pending and an authenticated admin advances it through
 * Processing → shipped → closed (or reverts to Pending). Illegal jumps are 409
 * and the transition endpoints reject unauthenticated callers. Rolled back per
 * test so the shared seed is untouched.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminOrderStatusMockMvcTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken() throws Exception {
        String body = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"1234\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }

    /** Places a guest order (product 8 / AirPods has ample seed stock) and returns its id. */
    private String placePendingOrder() throws Exception {
        String body = "{\"firstName\":\"Beka\",\"lastName\":\"Test\",\"email\":\"admin-flow@example.com\","
                + "\"address\":\"1 Rustaveli\",\"city\":\"Tbilisi\","
                + "\"items\":[{\"productId\":\"8\",\"qty\":1}]}";
        String res = mockMvc.perform(post("/shop/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderStatus").value("Pending"))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(res).get("id").asText();
    }

    @Test
    void fullLifecycle_processShipClose() throws Exception {
        String token = adminToken();
        String id = placePendingOrder();

        mockMvc.perform(put("/shop/order/" + id + "/process").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value("Processing"));

        mockMvc.perform(put("/shop/order/" + id + "/ship").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value("shipped"));

        mockMvc.perform(put("/shop/order/" + id + "/close").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value("closed"));
    }

    @Test
    void revert_processThenBackToPending() throws Exception {
        String token = adminToken();
        String id = placePendingOrder();

        mockMvc.perform(put("/shop/order/" + id + "/process").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value("Processing"));

        mockMvc.perform(put("/shop/order/" + id + "/pending").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value("Pending"));
    }

    @Test
    void illegalJump_pendingToShip_returns409() throws Exception {
        String token = adminToken();
        String id = placePendingOrder();

        // shipped is only reachable from Processing, so skipping Process is a conflict.
        mockMvc.perform(put("/shop/order/" + id + "/ship").header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }

    @Test
    void transition_withoutToken_is401() throws Exception {
        String id = placePendingOrder();

        mockMvc.perform(put("/shop/order/" + id + "/process"))
                .andExpect(status().isUnauthorized());
    }
}
