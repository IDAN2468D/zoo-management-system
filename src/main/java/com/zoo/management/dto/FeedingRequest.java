package com.zoo.management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class FeedingRequest {

    @NotBlank(message = "פריט המזון הינו שדה חובה")
    private String foodItem;

    @Positive(message = "כמות המזון חייבת להיות גדולה מ-0")
    private Double amountKg;

    private String fedBy;
    private String notes;

    public FeedingRequest() {
    }

    public FeedingRequest(String foodItem, Double amountKg, String fedBy, String notes) {
        this.foodItem = foodItem;
        this.amountKg = amountKg;
        this.fedBy = fedBy;
        this.notes = notes;
    }

    public String getFoodItem() {
        return foodItem;
    }

    public void setFoodItem(String foodItem) {
        this.foodItem = foodItem;
    }

    public Double getAmountKg() {
        return amountKg;
    }

    public void setAmountKg(Double amountKg) {
        this.amountKg = amountKg;
    }

    public String getFedBy() {
        return fedBy;
    }

    public void setFedBy(String fedBy) {
        this.fedBy = fedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
