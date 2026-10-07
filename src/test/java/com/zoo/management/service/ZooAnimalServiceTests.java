package com.zoo.management.service;

import com.zoo.management.dto.AnimalStatsResponse;
import com.zoo.management.dto.FeedingRequest;
import com.zoo.management.dto.MedicalRecordRequest;
import com.zoo.management.exception.AnimalValidationException;
import com.zoo.management.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ZooAnimalServiceTests {

    @Autowired
    private ZooService zooService;

    @Test
    @DisplayName("Should successfully load sample animals with rich attributes from JPA")
    void testSampleAnimalsLoaded() {
        List<Animal> animals = zooService.getAllAnimals();
        assertFalse(animals.isEmpty());
        assertTrue(animals.size() >= 15);

        Animal simba = animals.stream().filter(a -> "Simba".equals(a.getName())).findFirst().orElse(null);
        assertNotNull(simba);
        assertEquals(Species.FELINE, simba.getSpecies());
        assertEquals(SubSpecies.LION, simba.getSubSpecies());
        assertEquals(Gender.MALE, simba.getGender());
        assertEquals(DietType.CARNIVORE, simba.getDietType());
        assertNotNull(simba.getLastFedTime());
        assertFalse(simba.getFeedingHistory().isEmpty());
        assertFalse(simba.getMedicalHistory().isEmpty());
    }

    @Test
    @DisplayName("Should feed animal and update lastFedTime and feeding history in DB")
    void testFeedAnimal() {
        Animal simba = zooService.searchAnimals("Simba").get(0);
        int initialFeedings = simba.getFeedingHistory().size();

        FeedingRequest request = new FeedingRequest("בשר בקר טרי", 6.5, "מטפל ראשי", "האכלת צהריים מזינה");
        Animal updated = zooService.feedAnimal(simba.getId(), request);

        assertEquals(initialFeedings + 1, updated.getFeedingHistory().size());
        assertEquals("בשר בקר טרי", updated.getFeedingHistory().get(0).getFoodItem());
        assertEquals(6.5, updated.getFeedingHistory().get(0).getAmountKg());
        assertNotNull(updated.getLastFedTime());
    }

    @Test
    @DisplayName("Should record medical checkup and update animal health status in DB")
    void testMedicalCheckup() {
        Animal simba = zooService.searchAnimals("Simba").get(0);
        int initialRecords = simba.getMedicalHistory().size();

        MedicalRecordRequest request = new MedicalRecordRequest(
                HealthStatus.UNDER_OBSERVATION,
                "צליעה קלה ברגל שמאל",
                "חבישה ומנוחה ל-48 שעות",
                "ד\"ר רון כץ",
                "מעקב מחר בבוקר"
        );

        Animal updated = zooService.recordMedicalCheckup(simba.getId(), request);
        assertEquals(HealthStatus.UNDER_OBSERVATION, updated.getHealthStatus());
        assertEquals(initialRecords + 1, updated.getMedicalHistory().size());
        assertEquals("צליעה קלה ברגל שמאל", updated.getMedicalHistory().get(0).getDiagnosis());
    }

    @Test
    @DisplayName("Should quarantine animal and create quarantine medical log in DB")
    void testQuarantineAnimal() {
        Animal simba = zooService.searchAnimals("Simba").get(0);
        Animal quarantined = zooService.quarantineAnimal(simba.getId(), "חשד לשפעת", "ד\"ר שרה מילר");

        assertEquals(HealthStatus.QUARANTINED, quarantined.getHealthStatus());
        assertTrue(quarantined.getMedicalHistory().get(0).getDiagnosis().contains("בידוד"));
    }

    @Test
    @DisplayName("Should enforce cage species compatibility when assigning animal")
    void testCageSpeciesCompatibility() {
        Cage birdCage = zooService.getAllCages().stream()
                .filter(c -> c.getSpecies() == Species.BIRD)
                .findFirst()
                .orElseThrow();

        Animal simba = zooService.searchAnimals("Simba").get(0);

        // Simba is FELINE, cannot be put in BIRD cage!
        assertThrows(AnimalValidationException.class, () -> {
            zooService.transferAnimalToCage(simba.getId(), birdCage.getId());
        });
    }

    @Test
    @DisplayName("Should filter animals by species, diet, health and endangered status")
    void testFilteringAnimals() {
        List<Animal> felines = zooService.getAnimalsBySpecies(Species.FELINE);
        assertFalse(felines.isEmpty());
        assertTrue(felines.stream().allMatch(a -> a.getSpecies() == Species.FELINE));

        List<Animal> herbivores = zooService.getAnimalsByDiet(DietType.HERBIVORE);
        assertFalse(herbivores.isEmpty());
        assertTrue(herbivores.stream().allMatch(a -> a.getDietType() == DietType.HERBIVORE));

        List<Animal> endangered = zooService.getEndangeredAnimals();
        assertFalse(endangered.isEmpty());
        assertTrue(endangered.stream().allMatch(Animal::isEndangered));
    }

    @Test
    @DisplayName("Should search animals by query across Hebrew and English text")
    void testSearchAnimals() {
        List<Animal> simbaResults = zooService.searchAnimals("Simba");
        assertEquals(1, simbaResults.size());
        assertEquals("Simba", simbaResults.get(0).getName());

        List<Animal> lionResults = zooService.searchAnimals("אריה");
        assertFalse(lionResults.isEmpty());

        List<Animal> chipResults = zooService.searchAnimals("CHIP-FEL-101");
        assertEquals(1, chipResults.size());
    }

    @Test
    @DisplayName("Should calculate correct animal statistics")
    void testAnimalStats() {
        AnimalStatsResponse stats = zooService.getAnimalStats();
        assertNotNull(stats);
        assertTrue(stats.getTotalAnimals() >= 15);
        assertTrue(stats.getHealthyCount() > 0);
        assertTrue(stats.getEndangeredCount() > 0);
        assertTrue(stats.getAverageAge() > 0);
        assertTrue(stats.getAverageWeightKg() > 0);
        assertNotNull(stats.getSpeciesDistribution());
        assertNotNull(stats.getDietDistribution());
    }
}
