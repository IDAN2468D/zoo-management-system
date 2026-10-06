package com.zoo.management.model;

public enum HealthStatus {
    HEALTHY("בריא"),
    SICK("חולה"),
    INJURED("פצוע"),
    QUARANTINED("בבידוד"),
    UNDER_OBSERVATION("בהשגחה");

    private final String hebrewName;

    HealthStatus(String hebrewName) {
        this.hebrewName = hebrewName;
    }

    public String getHebrewName() {
        return hebrewName;
    }
}
