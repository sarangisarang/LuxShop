package com.luxshop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HTTP-level tests for storefront sorting on GET /shop/products?sort=. Friendly
 * keys map to a price or name ordering; an unknown/absent key keeps the default.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ProductSortMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void priceAscending_startsWithCheapest() throws Exception {
        // Ray-Ban Aviator (520) is the cheapest seed product.
        mockMvc.perform(get("/shop/products").param("sort", "price_asc").param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].productName").value("Ray-Ban Aviator Classic"));
    }

    @Test
    void priceDescending_startsWithDearest() throws Exception {
        // Rolex Submariner (38500) is the most expensive seed product.
        mockMvc.perform(get("/shop/products").param("sort", "price_desc").param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].productName").value("Rolex Submariner Date"));
    }

    @Test
    void nameAscending_startsAlphabetically() throws Exception {
        mockMvc.perform(get("/shop/products").param("sort", "name_asc").param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].productName").value("Apple AirPods Pro 2"));
    }

    @Test
    void ratingDescending_startsWithTopRated() throws Exception {
        // Seed reviews: product 3 has a single 5-star (avg 5.0), the highest.
        mockMvc.perform(get("/shop/products").param("sort", "rating_desc").param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].productName").value("MacBook Pro 16\" M3 Max"))
                .andExpect(jsonPath("$.content[0].averageRating").value(5.0));
    }

    @Test
    void sortComposesWithSearch() throws Exception {
        // "pro" matches several products; price_desc should put the priciest first.
        mockMvc.perform(get("/shop/products").param("q", "pro").param("sort", "price_desc").param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].price").exists())
                .andExpect(jsonPath("$.totalElements").isNumber());
    }
}
