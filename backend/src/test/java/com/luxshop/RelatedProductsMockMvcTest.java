package com.luxshop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * "You may also like": other products in the same category, excluding the
 * product itself, capped at four. Seed categories — Watches: 1, 2; Audio: 7, 8, 9.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RelatedProductsMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void related_returnsSameCategoryExcludingSelf() throws Exception {
        // Product 1 (Rolex, Watches) -> the other watch, Omega (2).
        mockMvc.perform(get("/shop/product/1/related"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem("2")))
                .andExpect(jsonPath("$[*].id", not(hasItem("1"))))
                .andExpect(jsonPath("$[*].category.name", hasItem("Watches")));
    }

    @Test
    void related_isCappedAtFourAndExcludesSelf() throws Exception {
        // Product 7 (Audio) -> 8 and 9, never itself, at most four.
        mockMvc.perform(get("/shop/product/7/related"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", lessThanOrEqualTo(4)))
                .andExpect(jsonPath("$[*].id", hasItem("8")))
                .andExpect(jsonPath("$[*].id", hasItem("9")))
                .andExpect(jsonPath("$[*].id", not(hasItem("7"))));
    }

    @Test
    void related_unknownProduct_is404() throws Exception {
        mockMvc.perform(get("/shop/product/nope/related"))
                .andExpect(status().isNotFound());
    }
}
