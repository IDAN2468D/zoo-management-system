package com.zoo.management.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "zoo_tasks")
public class ZooTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "assigned_role")
    private UserRole assignedRole = UserRole.KEEPER;

    @Column(name = "assigned_to_name")
    private String assignedToName;

    @Column(name = "animal_id")
    private Long animalId;

    @Column(name = "animal_name")
    private String animalName;

    @Column(nullable = false)
    private String priority = "MEDIUM"; // LOW, MEDIUM, HIGH, URGENT

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, IN_PROGRESS, COMPLETED

    @Column(name = "due_date")
    private String dueDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public ZooTask() {
    }

    public ZooTask(String title, String description, UserRole assignedRole, String assignedToName, String animalName, String priority, String dueDate) {
        this.title = title;
        this.description = description;
        this.assignedRole = assignedRole != null ? assignedRole : UserRole.KEEPER;
        this.assignedToName = assignedToName;
        this.animalName = animalName;
        this.priority = priority != null ? priority : "MEDIUM";
        this.status = "PENDING";
        this.dueDate = dueDate;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UserRole getAssignedRole() {
        return assignedRole;
    }

    public void setAssignedRole(UserRole assignedRole) {
        this.assignedRole = assignedRole;
    }

    public String getAssignedToName() {
        return assignedToName;
    }

    public void setAssignedToName(String assignedToName) {
        this.assignedToName = assignedToName;
    }

    public Long getAnimalId() {
        return animalId;
    }

    public void setAnimalId(Long animalId) {
        this.animalId = animalId;
    }

    public String getAnimalName() {
        return animalName;
    }

    public void setAnimalName(String animalName) {
        this.animalName = animalName;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        if ("COMPLETED".equalsIgnoreCase(status) && this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        } else if (!"COMPLETED".equalsIgnoreCase(status)) {
            this.completedAt = null;
        }
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
