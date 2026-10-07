package com.zoo.management.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feeding_records")
public class FeedingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private String foodItem;
    private Double amountKg;
    private String fedBy;

    @Column(length = 1000)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id")
    @JsonIgnore
    private Animal animal;

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

    public FeedingRecord(Long id, LocalDateTime timestamp, String foodItem, Double amountKg, String fedBy, String notes) {
        this.id = id;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.foodItem = foodItem;
        this.amountKg = amountKg;
        this.fedBy = fedBy;
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

    public Animal getAnimal() {
        return animal;
    }

    public void setAnimal(Animal animal) {
        this.animal = animal;
    }
}
