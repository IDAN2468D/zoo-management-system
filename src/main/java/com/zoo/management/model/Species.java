package com.zoo.management.model;

public enum Species {
    FELINE("חתוליים"),
    PRIMATE("קופים ופרימטים"),
    BIRD("עופות וציפורים"),
    REPTILE("זוחלים"),
    MAMMAL("יונקים"),
    AQUATIC("ימיים"),
    AMPHIBIAN("דו-חיים");

    private final String hebrewName;

    Species(String hebrewName) {
        this.hebrewName = hebrewName;
    }

    public String getHebrewName() {
        return hebrewName;
    }
}
