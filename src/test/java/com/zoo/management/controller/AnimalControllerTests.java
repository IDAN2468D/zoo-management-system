package com.zoo.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zoo.management.dto.FeedingRequest;
import com.zoo.management.dto.MedicalRecordRequest;
import com.zoo.management.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AnimalControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/animals - should return list of animals")
    void testGetAllAnimals() throws Exception {
        mockMvc.perform(get("/api/animals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(15))))
                .andExpect(jsonPath("$[0].name").exists());
    }

    @Test
    @DisplayName("GET /api/animals/{id} - should return animal by ID or 404")
    void testGetAnimalById() throws Exception {
        mockMvc.perform(get("/api/animals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        mockMvc.perform(get("/api/animals/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/animals - should create new animal")
    void testCreateAnimal() throws Exception {
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
    @DisplayName("POST /api/animals/{id}/feed - should feed animal and return updated object")
    void testFeedAnimal() throws Exception {
        FeedingRequest request = new FeedingRequest("סטייק עסיסי", 4.0, "דוד כהן", "האכלת בוקר");

        mockMvc.perform(post("/api/animals/1/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastFedTime").exists())
                .andExpect(jsonPath("$.feedingHistory[0].foodItem").value("סטייק עסיסי"));
    }

    @Test
    @DisplayName("POST /api/animals/{id}/medical - should record checkup and update health")
    void testMedicalCheckup() throws Exception {
        MedicalRecordRequest request = new MedicalRecordRequest(
                HealthStatus.UNDER_OBSERVATION,
                "בדיקת ראייה שגרתית",
                "טיפות עיניים",
                "ד\"ר שרה מילר",
                "בדיקה חוזרת בעוד שבוע"
        );

        mockMvc.perform(post("/api/animals/1/medical")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.healthStatus").value("UNDER_OBSERVATION"))
                .andExpect(jsonPath("$.medicalHistory[0].diagnosis").value("בדיקת ראייה שגרתית"));
    }

    @Test
    @DisplayName("GET /api/animals/stats - should return complete animal dashboard metrics")
    void testGetStats() throws Exception {
        mockMvc.perform(get("/api/animals/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAnimals").value(greaterThan(0)))
                .andExpect(jsonPath("$.speciesDistribution").isMap())
                .andExpect(jsonPath("$.healthDistribution").isMap())
                .andExpect(jsonPath("$.dietDistribution").isMap());
    }

    @Test
    @DisplayName("GET /api/animals/metadata - should return species and filter metadata")
    void testGetMetadata() throws Exception {
        mockMvc.perform(get("/api/animals/metadata"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.species").isArray())
                .andExpect(jsonPath("$.healthStatuses").isArray())
                .andExpect(jsonPath("$.dietTypes").isArray())
                .andExpect(jsonPath("$.conservationStatuses").isArray());
    }
}
