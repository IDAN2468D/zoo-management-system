package com.zoo.management.model;

import java.util.Objects;

public class Animal {
    private Long id;
    private String name;
    private Species species;
    private SubSpecies subSpecies;
    private HealthStatus healthStatus;
    private Cage cage;

    public Animal() {
    }

    public Animal(String name, Species species, SubSpecies subSpecies, HealthStatus healthStatus, Cage cage) {
        this.name = name;
        this.species = species;
        this.subSpecies = subSpecies;
        this.healthStatus = healthStatus;
        this.cage = cage;
    }

    public Animal(Long id, String name, Species species, SubSpecies subSpecies, HealthStatus healthStatus, Cage cage) {
        this.id = id;
        this.name = name;
        this.species = species;
        this.subSpecies = subSpecies;
        this.healthStatus = healthStatus;
        this.cage = cage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Species getSpecies() {
        return species;
    }

    public void setSpecies(Species species) {
        this.species = species;
    }

    public SubSpecies getSubSpecies() {
        return subSpecies;
    }

    public void setSubSpecies(SubSpecies subSpecies) {
        this.subSpecies = subSpecies;
        if (subSpecies != null && this.species == null) {
            this.species = subSpecies.getSpecies();
        }
    }

    public HealthStatus getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(HealthStatus healthStatus) {
        this.healthStatus = healthStatus;
    }

    public Cage getCage() {
        return cage;
    }

    public void setCage(Cage cage) {
        this.cage = cage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Animal animal = (Animal) o;
        return Objects.equals(id, animal.id) && Objects.equals(name, animal.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "Animal{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", species=" + species +
                ", subSpecies=" + subSpecies +
                ", healthStatus=" + healthStatus +
                ", cage=" + (cage != null ? cage.getId() : null) +
                '}';
    }
}
