package com.zoo.management.dto;

import java.util.Map;

public class AnimalStatsResponse {
    private int totalAnimals;
    private long healthyCount;
    private long sickOrInjuredCount;
    private long quarantinedCount;
    private long observationCount;
    private long endangeredCount;
    private long hungryCount;
    private double averageAge;
    private double averageWeightKg;
    private Map<String, Long> speciesDistribution;
    private Map<String, Long> healthDistribution;
    private Map<String, Long> dietDistribution;
    private Map<String, Long> conservationDistribution;

    public AnimalStatsResponse() {
    }

    public int getTotalAnimals() {
        return totalAnimals;
    }

    public void setTotalAnimals(int totalAnimals) {
        this.totalAnimals = totalAnimals;
    }

    public long getHealthyCount() {
        return healthyCount;
    }

    public void setHealthyCount(long healthyCount) {
        this.healthyCount = healthyCount;
    }

    public long getSickOrInjuredCount() {
        return sickOrInjuredCount;
    }

    public void setSickOrInjuredCount(long sickOrInjuredCount) {
        this.sickOrInjuredCount = sickOrInjuredCount;
    }

    public long getQuarantinedCount() {
        return quarantinedCount;
    }

    public void setQuarantinedCount(long quarantinedCount) {
        this.quarantinedCount = quarantinedCount;
    }

    public long getObservationCount() {
        return observationCount;
    }

    public void setObservationCount(long observationCount) {
        this.observationCount = observationCount;
    }

    public long getEndangeredCount() {
        return endangeredCount;
    }

    public void setEndangeredCount(long endangeredCount) {
        this.endangeredCount = endangeredCount;
    }

    public long getHungryCount() {
        return hungryCount;
    }

    public void setHungryCount(long hungryCount) {
        this.hungryCount = hungryCount;
    }

    public double getAverageAge() {
        return averageAge;
    }

    public void setAverageAge(double averageAge) {
        this.averageAge = averageAge;
    }

    public double getAverageWeightKg() {
        return averageWeightKg;
    }

    public void setAverageWeightKg(double averageWeightKg) {
        this.averageWeightKg = averageWeightKg;
    }

    public Map<String, Long> getSpeciesDistribution() {
        return speciesDistribution;
    }

    public void setSpeciesDistribution(Map<String, Long> speciesDistribution) {
        this.speciesDistribution = speciesDistribution;
    }

    public Map<String, Long> getHealthDistribution() {
        return healthDistribution;
    }

    public void setHealthDistribution(Map<String, Long> healthDistribution) {
        this.healthDistribution = healthDistribution;
    }

    public Map<String, Long> getDietDistribution() {
        return dietDistribution;
    }

    public void setDietDistribution(Map<String, Long> dietDistribution) {
        this.dietDistribution = dietDistribution;
    }

    public Map<String, Long> getConservationDistribution() {
        return conservationDistribution;
    }

    public void setConservationDistribution(Map<String, Long> conservationDistribution) {
        this.conservationDistribution = conservationDistribution;
    }
}
