package com.zoo.management.controller;

import com.zoo.management.model.Cage;
import com.zoo.management.service.ZooService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cages")
@CrossOrigin(origins = "*")
public class CageController {

    private final ZooService zooService;

    public CageController(ZooService zooService) {
        this.zooService = zooService;
    }

    @GetMapping
    public List<Cage> getAllCages() {
        return zooService.getAllCages();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cage> getCageById(@PathVariable Long id) {
        return zooService.getCageById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Cage> createCage(@RequestBody Cage cage) {
        Cage created = zooService.addCage(cage);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cage> updateCage(@PathVariable Long id, @RequestBody Cage cage) {
        return zooService.updateCage(id, cage)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCage(@PathVariable Long id) {
        boolean deleted = zooService.deleteCage(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
