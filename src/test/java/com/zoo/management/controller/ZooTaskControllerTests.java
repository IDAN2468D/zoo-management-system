package com.zoo.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zoo.management.model.UserRole;
import com.zoo.management.model.ZooTask;
import com.zoo.management.repository.ZooTaskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ZooTaskControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ZooTaskRepository taskRepository;

    private String basicAuthHeader(String user, String pass) {
        return "Basic " + Base64.getEncoder().encodeToString((user + ":" + pass).getBytes());
    }

    @Test
    @DisplayName("GET /api/tasks - החזרת רשימת כל המשימות (פתוח לקריאה)")
    void testGetAllTasks() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", not(empty())));
    }

    @Test
    @DisplayName("POST /api/tasks - יצירת משימה חדשה על ידי מטפל (Keeper)")
    void testCreateTaskAsKeeper() throws Exception {
        ZooTask task = new ZooTask("בדיקת טמפרטורה", "בדיקת חיישני אקלים בכלובי הזוחלים", UserRole.KEEPER, "מאיה", "Kaa", "MEDIUM", "מחר");

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", basicAuthHeader("keeper", "keeper123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title", is("בדיקת טמפרטורה")))
                .andExpect(jsonPath("$.priority", is("MEDIUM")));
    }

    @Test
    @DisplayName("PUT /api/tasks/{id}/status - עדכון סטטוס משימה ל-COMPLETED על ידי וטרינר")
    void testUpdateTaskStatus() throws Exception {
        ZooTask task = taskRepository.save(new ZooTask("טיפול שיניים", "בדיקה", UserRole.VET, "שרה", "Shere Khan", "HIGH", "היום"));

        mockMvc.perform(put("/api/tasks/" + task.getId() + "/status")
                        .header("Authorization", basicAuthHeader("vet", "vet123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "COMPLETED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COMPLETED")));
    }

    @Test
    @DisplayName("DELETE /api/tasks/{id} - מחיקת משימה על ידי מנהל (Admin) בהצלחה")
    void testDeleteTaskAsAdmin() throws Exception {
        ZooTask task = taskRepository.save(new ZooTask("משימה למחיקה", "תיאור", UserRole.ADMIN, "דוד", "Simba", "LOW", "היום"));

        mockMvc.perform(delete("/api/tasks/" + task.getId())
                        .header("Authorization", basicAuthHeader("admin", "admin123")))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/tasks/{id} - מחיקת משימה על ידי מטפל (Keeper) נחסמת (403 Forbidden)")
    void testDeleteTaskAsKeeperForbidden() throws Exception {
        ZooTask task = taskRepository.save(new ZooTask("משימה מוגנת", "תיאור", UserRole.ADMIN, "דוד", "Simba", "LOW", "היום"));

        mockMvc.perform(delete("/api/tasks/" + task.getId())
                        .header("Authorization", basicAuthHeader("keeper", "keeper123")))
                .andExpect(status().isForbidden());
    }
}
