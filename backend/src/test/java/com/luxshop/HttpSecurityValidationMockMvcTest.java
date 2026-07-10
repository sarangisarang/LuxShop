package com.luxshop;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HTTP-level tests for the security filter chain and Bean Validation: public
 * endpoints stay open, protected ones need a valid Bearer token, and invalid
 * request bodies come back as 400 with per-field messages.
 */
@SpringBootTest
@AutoConfigureMockMvc
class HttpSecurityValidationMockMvcTest {

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

    @Test
    void publicCatalog_isAccessibleWithoutToken() throws Exception {
        mockMvc.perform(get("/shop/products"))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpoint_withoutToken_is401() throws Exception {
        mockMvc.perform(get("/shop/order"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_withValidToken_is200() throws Exception {
        mockMvc.perform(get("/shop/order")
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpoint_withGarbageToken_is401() throws Exception {
        mockMvc.perform(get("/shop/order")
                        .header("Authorization", "Bearer not.a.jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidProduct_returns400WithFieldErrors() throws Exception {
        // Validation runs before the method body, so any categoryId path works.
        mockMvc.perform(post("/shop/product/any-id")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productName\":\"\",\"price\":-10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fields.productName").exists())
                .andExpect(jsonPath("$.fields.Price").exists())
                .andExpect(jsonPath("$.fields.Stock").exists());
    }

    @Test
    void invalidRegistration_returns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/register/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\",\"password\":\"123\",\"firstName\":\"\",\"lastName\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fields.email").exists())
                .andExpect(jsonPath("$.fields.password").exists());
    }
}
