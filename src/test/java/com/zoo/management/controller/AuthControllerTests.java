package com.zoo.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zoo.management.dto.LoginRequest;
import com.zoo.management.dto.RegisterRequest;
import com.zoo.management.model.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ==========================================
    // 1. בדיקות התחברות תקינה (Login Success)
    // ==========================================
    @Test
    @DisplayName("POST /api/auth/login - התחברות מוצלחת של מנהל מערכת (admin)")
    void testAdminLoginSuccess() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("admin123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.roleAuthority").value("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("POST /api/auth/login - התחברות מוצלחת של וטרינר (vet)")
    void testVetLoginSuccess() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("vet");
        req.setPassword("vet123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.username").value("vet"))
                .andExpect(jsonPath("$.role").value("VET"))
                .andExpect(jsonPath("$.roleAuthority").value("ROLE_VET"));
    }

    @Test
    @DisplayName("POST /api/auth/login - התחברות מוצלחת של מטפל (keeper)")
    void testKeeperLoginSuccess() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("keeper");
        req.setPassword("keeper123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.username").value("keeper"))
                .andExpect(jsonPath("$.role").value("KEEPER"))
                .andExpect(jsonPath("$.roleAuthority").value("ROLE_KEEPER"));
    }

    // ==========================================
    // 2. בדיקות התחברות שגויה (Bad Credentials)
    // ==========================================
    @Test
    @DisplayName("POST /api/auth/login - כישלון התחברות עקב סיסמה שגויה (401)")
    void testLoginWithWrongPassword() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("wrong_password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.authenticated").value(false))
                .andExpect(jsonPath("$.message", containsString("שגויים")));
    }

    @Test
    @DisplayName("POST /api/auth/login - כישלון התחברות של משתמש שלא קיים (401)")
    void testLoginWithNonExistentUser() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("user_does_not_exist");
        req.setPassword("secret123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.authenticated").value(false));
    }

    // ==========================================
    // 3. בדיקות הרשמת משתמש חדש (Registration)
    // ==========================================
    @Test
    @DisplayName("POST /api/auth/register - הרשמת משתמש חדש בהצלחה ולאחר מכן התחברות עמו")
    void testRegisterAndLoginNewUser() throws Exception {
        RegisterRequest regReq = new RegisterRequest();
        regReq.setFullName("איתן מזרחי");
        regReq.setUsername("eitan_keeper");
        regReq.setPassword("eitanPass2026");
        regReq.setRole(UserRole.KEEPER);

        // הרשמה
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.username").value("eitan_keeper"))
                .andExpect(jsonPath("$.fullName").value("איתן מזרחי"))
                .andExpect(jsonPath("$.role").value("KEEPER"));

        // בדיקה שהמשתמש החדש יכול כעת להתחבר
        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername("eitan_keeper");
        loginReq.setPassword("eitanPass2026");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.username").value("eitan_keeper"));
    }

    @Test
    @DisplayName("POST /api/auth/register - חסימת הרשמה אם שם המשתמש כבר קיים (400)")
    void testRegisterDuplicateUsernameFails() throws Exception {
        RegisterRequest regReq = new RegisterRequest();
        regReq.setFullName("התחזות למנהל");
        regReq.setUsername("admin"); // קיים כבר
        regReq.setPassword("newPass123");
        regReq.setRole(UserRole.ADMIN);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.authenticated").value(false))
                .andExpect(jsonPath("$.message", containsString("כבר תפוס")));
    }

    // ==========================================
    // 4. בדיקות נתיב המשתמש הנוכחי (/api/auth/me) והתנתקות
    // ==========================================
    @Test
    @DisplayName("GET /api/auth/me - החזרת פרטי המשתמש כאשר מועבר Basic Auth תקין")
    void testGetMeWithBasicAuth() throws Exception {
        String authHeader = "Basic " + Base64.getEncoder().encodeToString("admin:admin123".getBytes());

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("GET /api/auth/me - החזרת אורח (Guest) כשאין אימות")
    void testGetMeGuest() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(false))
                .andExpect(jsonPath("$.username").value("guest"));
    }

    @Test
    @DisplayName("POST /api/auth/logout - התנתקות מוצלחת מהמערכת")
    void testLogout() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(false));
    }
}
