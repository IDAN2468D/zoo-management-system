package com.zoo.management.dto;

import com.zoo.management.model.HealthStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MedicalRecordRequest {

    @NotNull(message = "מצב בריאותי הינו שדה חובה")
    private HealthStatus healthStatus;

    @NotBlank(message = "אבחנה רפואית הינה שדה חובה")
    private String diagnosis;

    private String treatment;
    private String performedBy;
    private String notes;

    public MedicalRecordRequest() {
    }

    public MedicalRecordRequest(HealthStatus healthStatus, String diagnosis, String treatment, String performedBy, String notes) {
        this.healthStatus = healthStatus;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.performedBy = performedBy;
        this.notes = notes;
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
