package com.zoo.management.controller;

import com.zoo.management.model.Employee;
import com.zoo.management.service.ZooService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "*")
public class EmployeeController {

    private final ZooService zooService;

    public EmployeeController(ZooService zooService) {
        this.zooService = zooService;
    }

    @GetMapping
    public List<Employee> getAllEmployees() {
        return zooService.getAllEmployees();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        return zooService.getEmployeeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee) {
        Employee created = zooService.addEmployee(employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @RequestBody Employee employee) {
        return zooService.updateEmployee(id, employee)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        boolean deleted = zooService.deleteEmployee(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/{employeeId}/cages/{cageId}")
    public ResponseEntity<Employee> assignCage(@PathVariable Long employeeId, @PathVariable Long cageId) {
        return zooService.assignCageToEmployee(employeeId, cageId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{employeeId}/cages/{cageId}")
    public ResponseEntity<Employee> removeCage(@PathVariable Long employeeId, @PathVariable Long cageId) {
        return zooService.removeCageFromEmployee(employeeId, cageId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
