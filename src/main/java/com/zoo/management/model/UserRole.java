package com.zoo.management.model;

public enum UserRole {
    ADMIN("מנהל מערכת", "ROLE_ADMIN"),
    VET("וטרינר", "ROLE_VET"),
    KEEPER("מטפל חיות", "ROLE_KEEPER");

    private final String hebrewTitle;
    private final String springRole;

    UserRole(String hebrewTitle, String springRole) {
        this.hebrewTitle = hebrewTitle;
        this.springRole = springRole;
    }

    public String getHebrewTitle() {
        return hebrewTitle;
    }

    public String getSpringRole() {
        return springRole;
    }
}
