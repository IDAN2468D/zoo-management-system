package com.zoo.management.model;

public enum ConservationStatus {
    LEAST_CONCERN("ללא חשש", false),
    NEAR_THREATENED("קרוב לסיכון", false),
    VULNERABLE("פגיע", true),
    ENDANGERED("בסכנת הכחדה", true),
    CRITICALLY_ENDANGERED("בסכנת הכחדה חמורה", true);

    private final String hebrewName;
    private final boolean endangered;

    ConservationStatus(String hebrewName, boolean endangered) {
        this.hebrewName = hebrewName;
        this.endangered = endangered;
    }

    public String getHebrewName() {
        return hebrewName;
    }

    public boolean isEndangered() {
        return endangered;
    }
}
