package com.luxshop;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Admin gallery management: list is public, add/delete need a token. Product 1
 * seeds two gallery images. Rolled back per test.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GalleryAdminMockMvcTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken() throws Exception {
        String body = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"1234\"}"))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }

    @Test
    void list_isPublic_andSeeded() throws Exception {
        mockMvc.perform(get("/shop/product/1/images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void add_withoutToken_is401() throws Exception {
        mockMvc.perform(post("/shop/product/1/images")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com/x.jpg\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void add_thenDelete_withToken() throws Exception {
        String token = adminToken();
        String created = mockMvc.perform(post("/shop/product/1/images")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com/new.jpg\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").value("https://example.com/new.jpg"))
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(created).get("id").asLong();

        mockMvc.perform(get("/shop/product/1/images"))
                .andExpect(jsonPath("$.length()").value(3));

        mockMvc.perform(delete("/shop/product/1/images/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/shop/product/1/images"))
                .andExpect(jsonPath("$.length()").value(2));
    }
}
