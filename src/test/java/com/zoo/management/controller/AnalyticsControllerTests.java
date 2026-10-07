package com.zoo.management.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AnalyticsControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/analytics - קבלת נתוני אנליטיקה ופילוחי התפלגות מלאים")
    void testGetAnalyticsOverview() throws Exception {
        mockMvc.perform(get("/api/analytics"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.speciesDistribution", notNullValue()))
                .andExpect(jsonPath("$.healthDistribution", notNullValue()))
                .andExpect(jsonPath("$.conservationDistribution", notNullValue()))
                .andExpect(jsonPath("$.cagesReport", notNullValue()));
    }
}
