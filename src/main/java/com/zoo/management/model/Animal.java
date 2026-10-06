package com.zoo.management.model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Animal {
    private Long id;
    private String name;
    private Species species;
    private SubSpecies subSpecies;
    private HealthStatus healthStatus = HealthStatus.HEALTHY;
    private Cage cage;

    // Upgraded animal attributes
    private Gender gender = Gender.UNKNOWN;
    private Integer age;
    private Double weightKg;
    private DietType dietType = DietType.OMNIVORE;
    private String favoriteFood;
    private String originCountry;
    private ConservationStatus conservationStatus = ConservationStatus.LEAST_CONCERN;
    private String microchipId;
    private String feedingSchedule;
    private LocalDateTime lastFedTime;
    private String notes;

    private List<MedicalRecord> medicalHistory = new ArrayList<>();
    private List<FeedingRecord> feedingHistory = new ArrayList<>();

    public Animal() {
    }

    public Animal(String name, Species species, SubSpecies subSpecies, HealthStatus healthStatus, Cage cage) {
        this.name = name;
        this.species = species;
        this.subSpecies = subSpecies;
        this.healthStatus = healthStatus != null ? healthStatus : HealthStatus.HEALTHY;
        this.cage = cage;
        if (subSpecies != null && this.species == null) {
            this.species = subSpecies.getSpecies();
        }
    }

    public Animal(Long id, String name, Species species, SubSpecies subSpecies, HealthStatus healthStatus, Cage cage) {
        this(name, species, subSpecies, healthStatus, cage);
        this.id = id;
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

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(Double weightKg) {
        this.weightKg = weightKg;
    }

    public DietType getDietType() {
        return dietType;
    }

    public void setDietType(DietType dietType) {
        this.dietType = dietType;
    }

    public String getFavoriteFood() {
        return favoriteFood;
    }

    public void setFavoriteFood(String favoriteFood) {
        this.favoriteFood = favoriteFood;
    }

    public String getOriginCountry() {
        return originCountry;
    }

    public void setOriginCountry(String originCountry) {
        this.originCountry = originCountry;
    }

    public ConservationStatus getConservationStatus() {
        return conservationStatus;
    }

    public void setConservationStatus(ConservationStatus conservationStatus) {
        this.conservationStatus = conservationStatus;
    }

    public String getMicrochipId() {
        return microchipId;
    }

    public void setMicrochipId(String microchipId) {
        this.microchipId = microchipId;
    }

    public String getFeedingSchedule() {
        return feedingSchedule;
    }

    public void setFeedingSchedule(String feedingSchedule) {
        this.feedingSchedule = feedingSchedule;
    }

    public LocalDateTime getLastFedTime() {
        return lastFedTime;
    }

    public void setLastFedTime(LocalDateTime lastFedTime) {
        this.lastFedTime = lastFedTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<MedicalRecord> getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(List<MedicalRecord> medicalHistory) {
        this.medicalHistory = medicalHistory != null ? medicalHistory : new ArrayList<>();
    }

    public List<FeedingRecord> getFeedingHistory() {
        return feedingHistory;
    }

    public void setFeedingHistory(List<FeedingRecord> feedingHistory) {
        this.feedingHistory = feedingHistory != null ? feedingHistory : new ArrayList<>();
    }

    public void addMedicalRecord(MedicalRecord record) {
        if (this.medicalHistory == null) {
            this.medicalHistory = new ArrayList<>();
        }
        this.medicalHistory.add(0, record); // newest first
        if (record.getHealthStatus() != null) {
            this.healthStatus = record.getHealthStatus();
        }
    }

    public void addFeedingRecord(FeedingRecord record) {
        if (this.feedingHistory == null) {
            this.feedingHistory = new ArrayList<>();
        }
        this.feedingHistory.add(0, record); // newest first
        this.lastFedTime = record.getTimestamp();
    }

    public boolean isNeedsFeeding(int hoursThreshold) {
        if (lastFedTime == null) {
            return true;
        }
        return ChronoUnit.HOURS.between(lastFedTime, LocalDateTime.now()) >= hoursThreshold;
    }

    public boolean isEndangered() {
        return conservationStatus != null && conservationStatus.isEndangered();
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
                ", gender=" + gender +
                ", age=" + age +
                ", weightKg=" + weightKg +
                ", dietType=" + dietType +
                ", conservationStatus=" + conservationStatus +
                ", microchipId='" + microchipId + '\'' +
                ", cage=" + (cage != null ? cage.getId() : null) +
                '}';
    }
}
