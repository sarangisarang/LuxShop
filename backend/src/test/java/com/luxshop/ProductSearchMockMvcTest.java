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
 * HTTP-level tests for storefront free-text search on GET /shop/products?q=.
 * Search matches the base name or description case-insensitively; a blank query
 * returns the full paginated catalog.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ProductSearchMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void searchByName_isCaseInsensitive() throws Exception {
        mockMvc.perform(get("/shop/products").param("q", "rolex"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].productName").value("Rolex Submariner Date"));
    }

    @Test
    void searchByDescription_matches() throws Exception {
        // "titanium" appears only in the iPhone description, not its name.
        mockMvc.perform(get("/shop/products").param("q", "titanium"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].productName").value("iPhone 15 Pro Max"));
    }

    @Test
    void searchNoMatch_returnsEmptyPage() throws Exception {
        mockMvc.perform(get("/shop/products").param("q", "zzznotaproduct"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void blankQuery_returnsFullCatalog() throws Exception {
        mockMvc.perform(get("/shop/products").param("q", "").param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(12));
    }
}
