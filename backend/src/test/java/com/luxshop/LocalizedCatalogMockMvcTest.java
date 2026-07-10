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
 * Verifies the catalog is returned in the request locale (Accept-Language),
 * falling back to English when no translation exists.
 */
@SpringBootTest
@AutoConfigureMockMvc
class LocalizedCatalogMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void product_isEnglishByDefault() throws Exception {
        mockMvc.perform(get("/shop/product/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("iPhone 15 Pro Max"))
                .andExpect(jsonPath("$.category.name").value("Smartphones"));
    }

    @Test
    void product_isGeorgianUnderKaLocale() throws Exception {
        mockMvc.perform(get("/shop/product/5").header("Accept-Language", "ka"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("აიფონ 15 პრო მაქსი"))
                .andExpect(jsonPath("$.category.name").value("სმარტფონები"));
    }

    @Test
    void productList_isLocalizedUnderKaLocale() throws Exception {
        // Product 1 (Rolex) has a ka translation; the paginated list should use it.
        mockMvc.perform(get("/shop/products?size=12").header("Accept-Language", "ka"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == '1')].productName").value("როლექს საბმარინერი"));
    }
}
