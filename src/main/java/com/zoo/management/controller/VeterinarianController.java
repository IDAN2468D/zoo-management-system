package com.zoo.management.controller;

import com.zoo.management.model.Veterinarian;
import com.zoo.management.service.ZooService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/veterinarians")
@CrossOrigin(origins = "*")
public class VeterinarianController {

    private final ZooService zooService;

    public VeterinarianController(ZooService zooService) {
        this.zooService = zooService;
    }

    @GetMapping
    public List<Veterinarian> getAllVeterinarians() {
        return zooService.getAllVeterinarians();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Veterinarian> getVeterinarianById(@PathVariable Long id) {
        return zooService.getVeterinarianById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Veterinarian> createVeterinarian(@RequestBody Veterinarian veterinarian) {
        Veterinarian created = zooService.addVeterinarian(veterinarian);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Veterinarian> updateVeterinarian(@PathVariable Long id, @RequestBody Veterinarian veterinarian) {
        return zooService.updateVeterinarian(id, veterinarian)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVeterinarian(@PathVariable Long id) {
        boolean deleted = zooService.deleteVeterinarian(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
