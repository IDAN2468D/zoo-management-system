package com.zoo.management.controller;

import com.zoo.management.model.*;
import com.zoo.management.repository.AnimalRepository;
import com.zoo.management.repository.CageRepository;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final AnimalRepository animalRepository;
    private final CageRepository cageRepository;

    public AnalyticsController(AnimalRepository animalRepository, CageRepository cageRepository) {
        this.animalRepository = animalRepository;
        this.cageRepository = cageRepository;
    }

    @GetMapping
    public Map<String, Object> getAnalyticsOverview() {
        List<Animal> animals = animalRepository.findAll();
        List<Cage> cages = cageRepository.findAll();

        Map<String, Object> result = new LinkedHashMap<>();

        // 1. התפלגות מחלקות / מינים
        Map<String, Map<String, Object>> speciesStats = new LinkedHashMap<>();
        for (Species s : Species.values()) {
            Map<String, Object> item = new HashMap<>();
            item.put("code", s.name());
            item.put("name", s.getHebrewName());
            item.put("count", 0L);
            speciesStats.put(s.name(), item);
        }
        for (Animal a : animals) {
            if (a.getSpecies() != null) {
                String key = a.getSpecies().name();
                Map<String, Object> item = speciesStats.get(key);
                if (item != null) {
                    item.put("count", ((Long) item.get("count")) + 1);
                }
            }
        }
        result.put("speciesDistribution", speciesStats.values());

        // 2. התפלגות סטטוס בריאותי
        Map<String, Map<String, Object>> healthStats = new LinkedHashMap<>();
        for (HealthStatus hs : HealthStatus.values()) {
            Map<String, Object> item = new HashMap<>();
            item.put("code", hs.name());
            item.put("name", hs.getHebrewName());
            item.put("count", 0L);
            healthStats.put(hs.name(), item);
        }
        for (Animal a : animals) {
            if (a.getHealthStatus() != null) {
                String key = a.getHealthStatus().name();
                Map<String, Object> item = healthStats.get(key);
                if (item != null) {
                    item.put("count", ((Long) item.get("count")) + 1);
                }
            }
        }
        result.put("healthDistribution", healthStats.values());

        // 3. התפלגות סוגי תזונה
        Map<String, Map<String, Object>> dietStats = new LinkedHashMap<>();
        for (DietType dt : DietType.values()) {
            Map<String, Object> item = new HashMap<>();
            item.put("code", dt.name());
            item.put("name", dt.getHebrewName());
            item.put("count", 0L);
            dietStats.put(dt.name(), item);
        }
        for (Animal a : animals) {
            if (a.getDietType() != null) {
                String key = a.getDietType().name();
                Map<String, Object> item = dietStats.get(key);
                if (item != null) {
                    item.put("count", ((Long) item.get("count")) + 1);
                }
            }
        }
        result.put("dietDistribution", dietStats.values());

        // 4. התפלגות סכנת הכחדה
        Map<String, Map<String, Object>> conservationStats = new LinkedHashMap<>();
        for (ConservationStatus cs : ConservationStatus.values()) {
            Map<String, Object> item = new HashMap<>();
            item.put("code", cs.name());
            item.put("name", cs.getHebrewName());
            item.put("isEndangered", cs.isEndangered());
            item.put("count", 0L);
            conservationStats.put(cs.name(), item);
        }
        for (Animal a : animals) {
            if (a.getConservationStatus() != null) {
                String key = a.getConservationStatus().name();
                Map<String, Object> item = conservationStats.get(key);
                if (item != null) {
                    item.put("count", ((Long) item.get("count")) + 1);
                }
            }
        }
        result.put("conservationDistribution", conservationStats.values());

        // 5. תפוסת כלובים מפורטת
        Map<Long, Long> animalsPerCage = new HashMap<>();
        for (Animal a : animals) {
            if (a.getCage() != null && a.getCage().getId() != null) {
                animalsPerCage.put(a.getCage().getId(), animalsPerCage.getOrDefault(a.getCage().getId(), 0L) + 1);
            }
        }

        List<Map<String, Object>> cageReports = new ArrayList<>();
        for (Cage c : cages) {
            Map<String, Object> report = new HashMap<>();
            long count = animalsPerCage.getOrDefault(c.getId(), 0L);
            int capacity = c.getCapacity() != null ? c.getCapacity() : 6;
            double percent = capacity > 0 ? (count * 100.0) / capacity : 0.0;

            report.put("id", c.getId());
            report.put("name", c.getName());
            report.put("species", c.getSpecies() != null ? c.getSpecies().name() : null);
            report.put("speciesHebrew", c.getSpecies() != null ? c.getSpecies().getHebrewName() : "כללי");
            report.put("capacity", capacity);
            report.put("currentCount", count);
            report.put("occupancyPercent", Math.round(percent * 10.0) / 10.0);
            report.put("temperature", c.getTemperatureCelsius() != null ? c.getTemperatureCelsius() : 24.0);
            report.put("humidity", c.getHumidityPercent() != null ? c.getHumidityPercent() : 55.0);
            report.put("zone", c.getLocationZone() != null ? c.getLocationZone() : "מרכז");
            report.put("status", c.getStatus() != null ? c.getStatus() : "ACTIVE");
            cageReports.add(report);
        }
        result.put("cagesReport", cageReports);

        return result;
    }
}
