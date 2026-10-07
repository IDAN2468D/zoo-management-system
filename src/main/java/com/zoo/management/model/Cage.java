package com.zoo.management.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "cages")
public class Cage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Species species;

    @Column(name = "cage_name")
    private String name;

    @Column(name = "capacity")
    private Integer capacity = 6;

    @Column(name = "temperature_celsius")
    private Double temperatureCelsius = 24.0;

    @Column(name = "humidity_percent")
    private Double humidityPercent = 55.0;

    @Column(name = "location_zone")
    private String locationZone;

    @Column(name = "status")
    private String status = "ACTIVE";

    public Cage() {
    }

    public Cage(Species species) {
        this.species = species;
        this.name = species != null ? "מתחם " + species.getHebrewName() : "מתחם כללי";
    }

    public Cage(Species species, String name, Integer capacity, Double temperatureCelsius, Double humidityPercent, String locationZone) {
        this.species = species;
        this.name = name;
        this.capacity = capacity != null ? capacity : 6;
        this.temperatureCelsius = temperatureCelsius != null ? temperatureCelsius : 24.0;
        this.humidityPercent = humidityPercent != null ? humidityPercent : 55.0;
        this.locationZone = locationZone;
        this.status = "ACTIVE";
    }

    public Cage(Long id, Species species) {
        this.id = id;
        this.species = species;
        this.name = species != null ? "מתחם " + species.getHebrewName() : "מתחם כללי";
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

    public String getName() {
        return name != null && !name.trim().isEmpty() ? name : (species != null ? "מתחם " + species.getHebrewName() + " #" + id : "כלוב #" + id);
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCapacity() {
        return capacity != null ? capacity : 6;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Double getTemperatureCelsius() {
        return temperatureCelsius != null ? temperatureCelsius : 24.0;
    }

    public void setTemperatureCelsius(Double temperatureCelsius) {
        this.temperatureCelsius = temperatureCelsius;
    }

    public Double getHumidityPercent() {
        return humidityPercent != null ? humidityPercent : 55.0;
    }

    public void setHumidityPercent(Double humidityPercent) {
        this.humidityPercent = humidityPercent;
    }

    public String getLocationZone() {
        return locationZone != null ? locationZone : "מתחם מרכזי";
    }

    public void setLocationZone(String locationZone) {
        this.locationZone = locationZone;
    }

    public String getStatus() {
        return status != null ? status : "ACTIVE";
    }

    public void setStatus(String status) {
        this.status = status;
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
                ", name='" + name + '\'' +
                ", capacity=" + capacity +
                '}';
    }
}

