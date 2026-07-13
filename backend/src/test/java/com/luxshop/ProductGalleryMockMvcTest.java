package com.luxshop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The single-product endpoint returns an image gallery (main image first, then
 * the seeded extras); the listing endpoint leaves it empty to avoid a per-row
 * lazy load.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ProductGalleryMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void singleProduct_hasGallery() throws Exception {
        mockMvc.perform(get("/shop/product/1"))
                .andExpect(status().isOk())
                // main imageUrl + two seeded gallery images.
                .andExpect(jsonPath("$.images.length()").value(3))
                .andExpect(jsonPath("$.images[0]").value(org.hamcrest.Matchers.notNullValue()))
                .andExpect(jsonPath("$.images", hasItem("https://picsum.photos/seed/lux-1-a/800/600")));
    }

    @Test
    void listing_hasNoGallery() throws Exception {
        mockMvc.perform(get("/shop/products").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].images.length()").value(0));
    }
}
