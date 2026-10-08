package com.zoo.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zoo.management.model.InventoryItem;
import com.zoo.management.repository.InventoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class InventoryControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InventoryRepository inventoryRepository;

    private Long getExistingItemId() {
        return inventoryRepository.findAll().stream().findFirst().map(InventoryItem::getId).orElse(1L);
    }

    @Test
    @DisplayName("GET /api/inventory - צפייה במלאי פתוחה לכל משתמש (ללא אימות)")
    void testGetInventoryPublic() throws Exception {
        mockMvc.perform(get("/api/inventory"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("POST /api/inventory - בקשה ללא אימות נחסמת עם 401")
    void testCreateInventoryUnauthenticatedBlocked() throws Exception {
        InventoryItem item = new InventoryItem("חסה טרייה", 40.0, 10.0, "ק\"ג");

        mockMvc.perform(post("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "keeper_user", roles = {"KEEPER"})
    @DisplayName("POST /api/inventory/{id}/restock - מטפל מורשה לחדש מלאי (200 OK)")
    void testRestockAsKeeper() throws Exception {
        Long id = getExistingItemId();
        Map<String, Double> payload = Map.of("amount", 25.0);

        mockMvc.perform(post("/api/inventory/" + id + "/restock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.quantity", greaterThan(20.0)));
    }

    @Test
    @WithMockUser(username = "keeper_user", roles = {"KEEPER"})
    @DisplayName("POST /api/inventory - מטפל אינו מורשה להוסיף פריט מלאי חדש (403 Forbidden)")
    void testCreateInventoryAsKeeperForbidden() throws Exception {
        InventoryItem item = new InventoryItem("דוחן", 15.0, 5.0, "ק\"ג");

        mockMvc.perform(post("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin_user", roles = {"ADMIN"})
    @DisplayName("POST /api/inventory - מנהל מורשה להוסיף פריט מלאי חדש (200 OK)")
    void testCreateInventoryAsAdmin() throws Exception {
        InventoryItem item = new InventoryItem("תפוחי עץ", 50.0, 10.0, "ק\"ג");

        mockMvc.perform(post("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("תפוחי עץ"))
                .andExpect(jsonPath("$.quantity").value(50.0));
    }

    @Test
    @WithMockUser(username = "admin_user", roles = {"ADMIN"})
    @DisplayName("PUT /api/inventory/{id} - מנהל מורשה לעדכן פריט מלאי (200 OK)")
    void testUpdateInventoryAsAdmin() throws Exception {
        Long id = getExistingItemId();
        InventoryItem updated = new InventoryItem("בשר בקר משובח", 130.0, 35.0, "ק\"ג");

        mockMvc.perform(put("/api/inventory/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("בשר בקר משובח"))
                .andExpect(jsonPath("$.quantity").value(130.0));
    }

    @Test
    @WithMockUser(username = "admin_user", roles = {"ADMIN"})
    @DisplayName("DELETE /api/inventory/{id} - מנהל מורשה למחוק פריט מלאי (204 No Content)")
    void testDeleteInventoryAsAdmin() throws Exception {
        InventoryItem item = inventoryRepository.save(new InventoryItem("פריט זמני למחיקה", 10.0, 2.0, "יח'"));

        mockMvc.perform(delete("/api/inventory/" + item.getId()))
                .andExpect(status().isNoContent());
    }
}
