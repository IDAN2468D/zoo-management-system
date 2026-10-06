package com.zoo.management.model;

import java.time.LocalDateTime;

public class FeedingRecord {
    private LocalDateTime timestamp;
    private String foodItem;
    private Double amountKg;
    private String fedBy;
    private String notes;

    public FeedingRecord() {
        this.timestamp = LocalDateTime.now();
    }

    public FeedingRecord(String foodItem, Double amountKg, String fedBy, String notes) {
        this.timestamp = LocalDateTime.now();
        this.foodItem = foodItem;
        this.amountKg = amountKg;
        this.fedBy = fedBy;
        this.notes = notes;
    }

    public FeedingRecord(LocalDateTime timestamp, String foodItem, Double amountKg, String fedBy, String notes) {
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.foodItem = foodItem;
        this.amountKg = amountKg;
        this.fedBy = fedBy;
        this.notes = notes;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
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
