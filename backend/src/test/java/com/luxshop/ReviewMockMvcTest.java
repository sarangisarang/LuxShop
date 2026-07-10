package com.luxshop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HTTP-level tests for public product reviews: anyone can read a product's
 * reviews (newest first) and leave one; invalid payloads are 400 with per-field
 * messages, and an unknown product is 404. Rolled back per test.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReviewMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listReviews_returnsSeededNewestFirst() throws Exception {
        mockMvc.perform(get("/shop/product/1/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(2)))
                // Anna (2026-06-20) is newer than Giorgi (2026-06-15).
                .andExpect(jsonPath("$[0].authorName").value("Anna"));
    }

    @Test
    void postReview_creates201_andShowsUpFirst() throws Exception {
        String body = "{\"authorName\":\"Tamar\",\"rating\":5,\"comment\":\"Flawless, highly recommend.\"}";
        mockMvc.perform(post("/shop/product/5/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authorName").value("Tamar"))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.id").exists());

        mockMvc.perform(get("/shop/product/5/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].authorName").value("Tamar"));
    }

    @Test
    void invalidReview_returns400WithFieldErrors() throws Exception {
        // Blank name and out-of-range rating.
        String body = "{\"authorName\":\"\",\"rating\":0}";
        mockMvc.perform(post("/shop/product/1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.authorName").exists())
                .andExpect(jsonPath("$.fields.rating").exists());
    }

    @Test
    void reviewForUnknownProduct_returns404() throws Exception {
        mockMvc.perform(get("/shop/product/does-not-exist/reviews"))
                .andExpect(status().isNotFound());
    }
}
