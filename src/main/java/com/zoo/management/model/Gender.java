package com.zoo.management.model;

public enum Gender {
    MALE("זכר"),
    FEMALE("נקבה"),
    UNKNOWN("לא ידוע");

    private final String hebrewName;

    Gender(String hebrewName) {
        this.hebrewName = hebrewName;
    }

    public String getHebrewName() {
        return hebrewName;
    }
}
