package com.zoo.management.model;

public enum DietType {
    CARNIVORE("טורף"),
    HERBIVORE("צמחוני"),
    OMNIVORE("אוכל-כל"),
    INSECTIVORE("אוכל חרקים"),
    PISCIVORE("אוכל דגים");

    private final String hebrewName;

    DietType(String hebrewName) {
        this.hebrewName = hebrewName;
    }

    public String getHebrewName() {
        return hebrewName;
    }
}
