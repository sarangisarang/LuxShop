package com.luxshop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The catalog listing and single-product endpoints carry an aggregate rating so
 * the storefront can show stars on cards. Product 1 has two seed reviews (5, 4);
 * product 2 has none.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ProductRatingMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listExposesAverageAndCount() throws Exception {
        mockMvc.perform(get("/shop/products").param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id=='1')].averageRating", hasItem(4.5)))
                .andExpect(jsonPath("$.content[?(@.id=='1')].reviewCount", hasItem(2)));
    }

    @Test
    void productWithoutReviews_hasZeroCount() throws Exception {
        mockMvc.perform(get("/shop/products").param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id=='2')].reviewCount", contains(0)));
    }

    @Test
    void singleProductExposesRating() throws Exception {
        mockMvc.perform(get("/shop/product/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(4.5))
                .andExpect(jsonPath("$.reviewCount").value(2));
    }
}
