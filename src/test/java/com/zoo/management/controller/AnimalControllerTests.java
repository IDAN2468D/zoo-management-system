package com.zoo.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zoo.management.dto.FeedingRequest;
import com.zoo.management.dto.MedicalRecordRequest;
import com.zoo.management.model.*;
import com.zoo.management.repository.AnimalRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AnimalControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AnimalRepository animalRepository;

    private Long getExistingAnimalId() {
        return animalRepository.findAll().stream().findFirst().map(Animal::getId).orElse(1L);
    }

    // ==========================================
    // Public (Unauthenticated) Access Tests
    // ==========================================
    @Test
    @DisplayName("GET /api/animals - unauthenticated user should be able to view animals list")
    void testGetAllAnimalsPublic() throws Exception {
        mockMvc.perform(get("/api/animals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(15))))
                .andExpect(jsonPath("$[0].name").exists());
    }

    @Test
    @DisplayName("GET /api/animals/{id} - unauthenticated user can view animal by ID")
    void testGetAnimalByIdPublic() throws Exception {
        Long id = getExistingAnimalId();
        mockMvc.perform(get("/api/animals/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(get("/api/animals/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/animals/stats - public dashboard stats access")
    void testGetStatsPublic() throws Exception {
        mockMvc.perform(get("/api/animals/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAnimals").value(greaterThan(0)))
                .andExpect(jsonPath("$.speciesDistribution").isMap())
                .andExpect(jsonPath("$.healthDistribution").isMap())
                .andExpect(jsonPath("$.dietDistribution").isMap());
    }

    @Test
    @DisplayName("GET /api/animals/metadata - public metadata access")
    void testGetMetadataPublic() throws Exception {
        mockMvc.perform(get("/api/animals/metadata"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.species").isArray())
                .andExpect(jsonPath("$.healthStatuses").isArray())
                .andExpect(jsonPath("$.dietTypes").isArray())
                .andExpect(jsonPath("$.conservationStatuses").isArray());
    }

    @Test
    @DisplayName("GET /api/auth/me - unauthenticated returns guest status")
    void testAuthMeUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(false))
                .andExpect(jsonPath("$.username").value("guest"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /api/auth/me - authenticated admin returns role ADMIN")
    void testAuthMeAdmin() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"));
    }

    // ==========================================
    // RBAC: Animal Creation & Admin Operations
    // ==========================================
    @Test
    @DisplayName("POST /api/animals - unauthenticated should fail with 401")
    void testCreateAnimalUnauthenticatedFails() throws Exception {
        Animal animal = new Animal();
        animal.setName("Alex");
        animal.setSpecies(Species.FELINE);

        mockMvc.perform(post("/api/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(animal)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "KEEPER")
    @DisplayName("POST /api/animals - keeper should be forbidden (403)")
    void testCreateAnimalKeeperForbidden() throws Exception {
        Animal animal = new Animal();
        animal.setName("Alex");
        animal.setSpecies(Species.FELINE);

        mockMvc.perform(post("/api/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(animal)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/animals - admin should successfully create animal (201)")
    void testCreateAnimalAdminSuccess() throws Exception {
        Animal animal = new Animal();
        animal.setName("Alex");
        animal.setSpecies(Species.FELINE);
        animal.setSubSpecies(SubSpecies.LION);
        animal.setGender(Gender.MALE);
        animal.setAge(5);
        animal.setWeightKg(185.0);
        animal.setDietType(DietType.CARNIVORE);
        animal.setConservationStatus(ConservationStatus.VULNERABLE);
        animal.setMicrochipId("CHIP-NEW-999");

        mockMvc.perform(post("/api/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(animal)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Alex"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/animals/{id} - admin can delete animal")
    void testDeleteAnimalAdminSuccess() throws Exception {
        Long id = getExistingAnimalId();
        mockMvc.perform(delete("/api/animals/" + id))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "KEEPER")
    @DisplayName("DELETE /api/animals/{id} - keeper cannot delete animal (403)")
    void testDeleteAnimalKeeperForbidden() throws Exception {
        Long id = getExistingAnimalId();
        mockMvc.perform(delete("/api/animals/" + id))
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // RBAC: Feeding Operations (KEEPER & ADMIN)
    // ==========================================
    @Test
    @DisplayName("POST /api/animals/{id}/feed - unauthenticated should fail (401)")
    void testFeedAnimalUnauthenticatedFails() throws Exception {
        Long id = getExistingAnimalId();
        FeedingRequest request = new FeedingRequest("סטייק עסיסי", 4.0, "דוד כהן", "האכלת בוקר");

        mockMvc.perform(post("/api/animals/" + id + "/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "KEEPER")
    @DisplayName("POST /api/animals/{id}/feed - keeper should successfully feed animal (200)")
    void testFeedAnimalKeeperSuccess() throws Exception {
        Long id = getExistingAnimalId();
        FeedingRequest request = new FeedingRequest("סטייק עסיסי", 4.0, "מאיה לוי", "האכלת בוקר");

        mockMvc.perform(post("/api/animals/" + id + "/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastFedTime").exists())
                .andExpect(jsonPath("$.feedingHistory[0].foodItem").value("סטייק עסיסי"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/animals/{id}/feed - admin should also be allowed to feed animal (200)")
    void testFeedAnimalAdminSuccess() throws Exception {
        Long id = getExistingAnimalId();
        FeedingRequest request = new FeedingRequest("דגים", 2.0, "מנהל", "האכלה");

        mockMvc.perform(post("/api/animals/" + id + "/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "VET")
    @DisplayName("POST /api/animals/{id}/feed - vet alone is not authorized to feed (403)")
    void testFeedAnimalVetForbidden() throws Exception {
        Long id = getExistingAnimalId();
        FeedingRequest request = new FeedingRequest("בשר", 1.0, "וטרינר", "האכלה");

        mockMvc.perform(post("/api/animals/" + id + "/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // RBAC: Medical Operations (VET & ADMIN)
    // ==========================================
    @Test
    @WithMockUser(roles = "KEEPER")
    @DisplayName("POST /api/animals/{id}/medical - keeper cannot perform medical checkup (403)")
    void testMedicalCheckupKeeperForbidden() throws Exception {
        Long id = getExistingAnimalId();
        MedicalRecordRequest request = new MedicalRecordRequest(
                HealthStatus.UNDER_OBSERVATION,
                "בדיקה",
                "טיפול",
                "מטפל",
                "הערה"
        );

        mockMvc.perform(post("/api/animals/" + id + "/medical")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VET")
    @DisplayName("POST /api/animals/{id}/medical - vet can record checkup (200)")
    void testMedicalCheckupVetSuccess() throws Exception {
        Long id = getExistingAnimalId();
        MedicalRecordRequest request = new MedicalRecordRequest(
                HealthStatus.UNDER_OBSERVATION,
                "בדיקת ראייה שגרתית",
                "טיפות עיניים",
                "ד\"ר שרה מילר",
                "בדיקה חוזרת בעוד שבוע"
        );

        mockMvc.perform(post("/api/animals/" + id + "/medical")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.healthStatus").value("UNDER_OBSERVATION"))
                .andExpect(jsonPath("$.medicalHistory[0].diagnosis").value("בדיקת ראייה שגרתית"));
    }

    @Test
    @WithMockUser(roles = "VET")
    @DisplayName("POST /api/animals/{id}/quarantine - vet can quarantine animal (200)")
    void testQuarantineVetSuccess() throws Exception {
        Long id = getExistingAnimalId();
        mockMvc.perform(post("/api/animals/" + id + "/quarantine")
                        .param("reason", "חשד למחלה מידבקת")
                        .param("vetName", "ד\"ר שרה"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.healthStatus").value("QUARANTINED"));
    }

    @Test
    @WithMockUser(roles = "KEEPER")
    @DisplayName("POST /api/animals/{id}/quarantine - keeper cannot quarantine animal (403)")
    void testQuarantineKeeperForbidden() throws Exception {
        Long id = getExistingAnimalId();
        mockMvc.perform(post("/api/animals/" + id + "/quarantine")
                        .param("reason", "חשד למחלה מידבקת"))
                .andExpect(status().isForbidden());
    }
}
