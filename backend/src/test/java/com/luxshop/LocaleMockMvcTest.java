package com.luxshop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies that validation and error messages are localized from the
 * Accept-Language header (English by default, Georgian for "ka").
 */
@SpringBootTest
@AutoConfigureMockMvc
class LocaleMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void messages_areEnglishByDefault() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fields.username").value("Username is required"));
    }

    @Test
    void validationMessages_areLocalizedForGeorgian() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .header("Accept-Language", "ka")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ვალიდაცია ვერ გაიარა"))
                .andExpect(jsonPath("$.fields.username").value("მომხმარებლის სახელი აუცილებელია"));
    }

    @Test
    void authError_isLocalizedForGeorgian() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .header("Accept-Language", "ka")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("არასწორი მომხმარებელი ან პაროლი"));
    }
}
