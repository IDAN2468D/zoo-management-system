package com.zoo.management.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Employee {
    private Long id;
    private String name;
    private Role role;
    private List<Cage> assignedCages = new ArrayList<>();

    public Employee() {
    }

    public Employee(String name, Role role) {
        this.name = name;
        this.role = role;
        this.assignedCages = new ArrayList<>();
    }

    public Employee(Long id, String name, Role role) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.assignedCages = new ArrayList<>();
    }

    public Employee(String name, Role role, List<Cage> assignedCages) {
        this.name = name;
        this.role = role;
        this.assignedCages = assignedCages != null ? assignedCages : new ArrayList<>();
    }

    public Employee(Long id, String name, Role role, List<Cage> assignedCages) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.assignedCages = assignedCages != null ? assignedCages : new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public List<Cage> getAssignedCages() {
        return assignedCages;
    }

    public void setAssignedCages(List<Cage> assignedCages) {
        this.assignedCages = assignedCages != null ? assignedCages : new ArrayList<>();
    }

    public void assignCage(Cage cage) {
        if (cage != null && !this.assignedCages.contains(cage)) {
            this.assignedCages.add(cage);
        }
    }

    public void removeCage(Cage cage) {
        if (cage != null) {
            this.assignedCages.remove(cage);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return Objects.equals(id, employee.id) && Objects.equals(name, employee.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", role=" + role +
                ", assignedCagesCount=" + assignedCages.size() +
                '}';
    }
}
