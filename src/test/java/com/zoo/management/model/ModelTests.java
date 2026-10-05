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
        assertEquals(3, HealthStatus.values().length);
        assertNotNull(HealthStatus.valueOf("HEALTHY"));
        assertNotNull(Role.valueOf("MANAGER"));
    }
}
