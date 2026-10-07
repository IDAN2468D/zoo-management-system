package com.zoo.management.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ModelTests {

    @Test
    @DisplayName("Should create Animal and check attributes and auto-species fallback")
    void testAnimalCreation() {
        Cage cage = new Cage(1L, Species.FELINE);
        Animal simba = new Animal("Simba", Species.FELINE, SubSpecies.LION, HealthStatus.HEALTHY, cage);
        simba.setId(10L);

        assertEquals(10L, simba.getId());
        assertEquals("Simba", simba.getName());
        assertEquals(Species.FELINE, simba.getSpecies());
        assertEquals(SubSpecies.LION, simba.getSubSpecies());
        assertEquals(HealthStatus.HEALTHY, simba.getHealthStatus());
        assertEquals(cage, simba.getCage());

        // Test auto-species fallback when setting subSpecies
        Animal eagle = new Animal();
        eagle.setName("Majestic");
        eagle.setSubSpecies(SubSpecies.EAGLE);
        assertEquals(Species.BIRD, eagle.getSpecies());
        assertEquals(SubSpecies.EAGLE.getSpecies(), eagle.getSpecies());
        assertEquals(SubSpecies.EAGLE.species(), eagle.getSpecies());
    }

    @Test
    @DisplayName("Should verify Cage properties and equals/hashCode")
    void testCage() {
        Cage cage1 = new Cage(1L, Species.PRIMATE);
        Cage cage2 = new Cage(1L, Species.PRIMATE);
        Cage cage3 = new Cage(2L, Species.BIRD);

        assertEquals(cage1, cage2);
        assertEquals(cage1.hashCode(), cage2.hashCode());
        assertNotEquals(cage1, cage3);
        assertTrue(cage1.toString().contains("PRIMATE"));
    }

    @Test
    @DisplayName("Should assign and remove cages for Employee")
    void testEmployeeAssignCages() {
        Employee keeper = new Employee(1L, "David", Role.EMPLOYEE);
        Cage monkeyCage = new Cage(101L, Species.PRIMATE);
        Cage lionCage = new Cage(102L, Species.FELINE);

        keeper.assignCage(monkeyCage);
        keeper.assignCage(lionCage);
        // Duplicate assignment should not duplicate in list
        keeper.assignCage(monkeyCage);

        assertEquals(2, keeper.getAssignedCages().size());
        assertTrue(keeper.getAssignedCages().contains(monkeyCage));

        keeper.removeCage(monkeyCage);
        assertEquals(1, keeper.getAssignedCages().size());
        assertFalse(keeper.getAssignedCages().contains(monkeyCage));
    }

    @Test
    @DisplayName("Should verify Veterinarian creation and specialization")
    void testVeterinarian() {
        Veterinarian vet = new Veterinarian(1L, "Dr. Sarah", Species.AQUATIC, "sarah@zoo.com", "050-1234567");

        assertEquals(1L, vet.getId());
        assertEquals("Dr. Sarah", vet.getName());
        assertEquals(Species.AQUATIC, vet.getSpecialization());
        assertEquals("sarah@zoo.com", vet.getEmail());
        assertEquals("050-1234567", vet.getPhone());
        assertTrue(vet.toString().contains("Dr. Sarah"));
    }

    @Test
    @DisplayName("Should verify SubSpecies mapping to Species")
    void testSubSpeciesMapping() {
        assertEquals(Species.FELINE, SubSpecies.LION.getSpecies());
        assertEquals(Species.PRIMATE, SubSpecies.CHIMPANZEE.getSpecies());
        assertEquals(Species.BIRD, SubSpecies.PARROT.getSpecies());
        assertEquals(Species.REPTILE, SubSpecies.CROCODILE.getSpecies());
        assertEquals(Species.MAMMAL, SubSpecies.ELEPHANT.getSpecies());
        assertEquals(Species.AQUATIC, SubSpecies.DOLPHIN.getSpecies());
    }

    @Test
    @DisplayName("Should verify Role and HealthStatus enums")
    void testEnums() {
        assertEquals(2, Role.values().length);
        assertEquals(5, HealthStatus.values().length);
        assertNotNull(HealthStatus.valueOf("HEALTHY"));
        assertNotNull(HealthStatus.valueOf("QUARANTINED"));
        assertNotNull(Role.valueOf("MANAGER"));
    }

    @Test
    @DisplayName("Should verify new Animal attributes, feeding and medical records")
    void testEnhancedAnimalAttributes() {
        Animal lion = new Animal();
        lion.setName("Simba");
        lion.setSpecies(Species.FELINE);
        lion.setSubSpecies(SubSpecies.LION);
        lion.setGender(Gender.MALE);
        lion.setAge(6);
        lion.setWeightKg(190.5);
        lion.setDietType(DietType.CARNIVORE);
        lion.setConservationStatus(ConservationStatus.VULNERABLE);
        lion.setMicrochipId("CHIP-12345");

        assertTrue(lion.isEndangered());
        assertEquals("זכר", lion.getGender().getHebrewName());
        assertEquals("טורף", lion.getDietType().getHebrewName());

        // Feeding test
        FeedingRecord feeding = new FeedingRecord("Meat", 5.0, "Maya", "Lunch");
        lion.addFeedingRecord(feeding);
        assertNotNull(lion.getLastFedTime());
        assertEquals(1, lion.getFeedingHistory().size());
        assertFalse(lion.isNeedsFeeding(2));

        // Medical test
        MedicalRecord med = new MedicalRecord(1L, HealthStatus.UNDER_OBSERVATION, "Checkup", "Vitamins", "Dr. Sarah", "All good");
        lion.addMedicalRecord(med);
        assertEquals(HealthStatus.UNDER_OBSERVATION, lion.getHealthStatus());
        assertEquals(1, lion.getMedicalHistory().size());

        // Image URL test
        lion.setImageUrl("images/simba.jpg");
        assertEquals("images/simba.jpg", lion.getImageUrl());
    }

    @Test
    @DisplayName("Should verify enhanced Cage climate and capacity properties")
    void testEnhancedCageProperties() {
        Cage cage = new Cage(10L, Species.FELINE);
        cage.setName("Savanna Enclosure A");
        cage.setCapacity(8);
        cage.setTemperatureCelsius(26.5);
        cage.setHumidityPercent(45.0);
        cage.setLocationZone("East Zone");
        cage.setStatus("ACTIVE");

        assertEquals(10L, cage.getId());
        assertEquals("Savanna Enclosure A", cage.getName());
        assertEquals(8, cage.getCapacity());
        assertEquals(26.5, cage.getTemperatureCelsius());
        assertEquals(45.0, cage.getHumidityPercent());
        assertEquals("East Zone", cage.getLocationZone());
        assertEquals("ACTIVE", cage.getStatus());
    }

    @Test
    @DisplayName("Should verify ZooTask creation, status progression, and fields")
    void testZooTaskProperties() {
        ZooTask task = new ZooTask("Morning Feeding", "Feed meat to lions", UserRole.KEEPER, "Maya Levi", "Simba", "HIGH", "Today 09:00");
        task.setId(100L);

        assertEquals(100L, task.getId());
        assertEquals("Morning Feeding", task.getTitle());
        assertEquals("Feed meat to lions", task.getDescription());
        assertEquals(UserRole.KEEPER, task.getAssignedRole());
        assertEquals("Maya Levi", task.getAssignedToName());
        assertEquals("Simba", task.getAnimalName());
        assertEquals("HIGH", task.getPriority());
        assertEquals("PENDING", task.getStatus());
        assertEquals("Today 09:00", task.getDueDate());

        task.setStatus("COMPLETED");
        assertEquals("COMPLETED", task.getStatus());
    }
}
