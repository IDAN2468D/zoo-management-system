package com.zoo.management.model;

import java.time.LocalDateTime;

public class MedicalRecord {
    private Long id;
    private LocalDateTime timestamp;
    private HealthStatus healthStatus;
    private String diagnosis;
    private String treatment;
    private String performedBy;
    private String notes;

    public MedicalRecord() {
        this.timestamp = LocalDateTime.now();
    }

    public MedicalRecord(Long id, HealthStatus healthStatus, String diagnosis, String treatment, String performedBy, String notes) {
        this.id = id;
        this.timestamp = LocalDateTime.now();
        this.healthStatus = healthStatus;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.performedBy = performedBy;
        this.notes = notes;
    }

    public MedicalRecord(Long id, LocalDateTime timestamp, HealthStatus healthStatus, String diagnosis, String treatment, String performedBy, String notes) {
        this.id = id;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.healthStatus = healthStatus;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.performedBy = performedBy;
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public HealthStatus getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(HealthStatus healthStatus) {
        this.healthStatus = healthStatus;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
