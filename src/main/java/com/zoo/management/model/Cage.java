package com.zoo.management.model;

import java.util.Objects;

public class Cage {
    private Long id;
    private Species species;

    public Cage() {
    }

    public Cage(Species species) {
        this.species = species;
    }

    public Cage(Long id, Species species) {
        this.id = id;
        this.species = species;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Species getSpecies() {
        return species;
    }

    public void setSpecies(Species species) {
        this.species = species;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cage cage = (Cage) o;
        return Objects.equals(id, cage.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Cage{" +
                "id=" + id +
                ", species=" + species +
                '}';
    }
}
