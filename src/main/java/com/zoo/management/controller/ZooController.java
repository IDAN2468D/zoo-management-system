package com.zoo.management.controller;

import com.zoo.management.service.ZooService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ZooController {

    private final ZooService zooService;

    public ZooController(ZooService zooService) {
        this.zooService = zooService;
    }

    @GetMapping("/overview")
    public Map<String, Object> getZooOverview() {
        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("systemName", "Zoo Management System");
        overview.put("status", "ACTIVE");
        overview.put("totalAnimals", zooService.getAllAnimals().size());
        overview.put("totalCages", zooService.getAllCages().size());
        overview.put("totalEmployees", zooService.getAllEmployees().size());
        overview.put("totalVeterinarians", zooService.getAllVeterinarians().size());
        return overview;
    }
}
