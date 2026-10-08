package com.zoo.management.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_items")
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // למשל: "בשר בקר טרי", "במבוק", "דגים", "חציר"

    @Column(nullable = false)
    private Double quantity; // כמות נוכחית במלאי

    @Column(nullable = false)
    private Double minThreshold; // סף מינימום שממנו ומטה מקבלים התראה

    private String unit; // יחידת מידה: "ק\"ג", "שקים", "יחידות"

    private LocalDateTime lastRestocked;

    public InventoryItem() {
        this.lastRestocked = LocalDateTime.now();
    }

    public InventoryItem(String name, Double quantity, Double minThreshold, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.minThreshold = minThreshold;
        this.unit = unit != null ? unit : "ק\"ג";
        this.lastRestocked = LocalDateTime.now();
    }

    // האם הפריט במלאי נמוך
    public boolean isLowStock() {
        return quantity != null && minThreshold != null && quantity <= minThreshold;
    }

    // הפחתת כמות בעת האכלה
    public boolean deduct(Double amount) {
        if (amount == null || amount <= 0) return true;
        if (this.quantity < amount) {
            this.quantity = 0.0;
            return false; // המלאי נגמר לגמרי
        }
        this.quantity -= amount;
        return true;
    }

    // הוספת מלאי (Restock)
    public void addStock(Double amount) {
        if (amount != null && amount > 0) {
            this.quantity += amount;
            this.lastRestocked = LocalDateTime.now();
        }
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }

    public Double getMinThreshold() { return minThreshold; }
    public void setMinThreshold(Double minThreshold) { this.minThreshold = minThreshold; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public LocalDateTime getLastRestocked() { return lastRestocked; }
    public void setLastRestocked(LocalDateTime lastRestocked) { this.lastRestocked = lastRestocked; }
}