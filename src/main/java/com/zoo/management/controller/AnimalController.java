package com.zoo.management.controller;

import com.zoo.management.dto.AnimalStatsResponse;
import com.zoo.management.dto.FeedingRequest;
import com.zoo.management.dto.MedicalRecordRequest;
import com.zoo.management.model.*;
import com.zoo.management.service.ZooService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/animals")
@CrossOrigin(origins = "*")
public class AnimalController {

    private final ZooService zooService;

    public AnimalController(ZooService zooService) {
        this.zooService = zooService;
    }

    // 1. קבלת כל החיות עם אפשרות סינון רב-שכבתית
    @GetMapping
    public List<Animal> getAnimals(
            @RequestParam(required = false) Species species,
            @RequestParam(required = false) HealthStatus health,
            @RequestParam(required = false) DietType diet,
            @RequestParam(required = false) Boolean endangered,
            @RequestParam(required = false) Long cageId,
            @RequestParam(required = false) String q) {

        if (species != null || health != null || diet != null || endangered != null || cageId != null || (q != null && !q.isBlank())) {
            return zooService.filterAnimals(species, health, diet, endangered, cageId, q);
        }
        return zooService.getAllAnimals();
    }

    // 2. לוח מחוונים וסטטיסטיקות מתקדמות על כל החיות (נתיב סטטי שמופיע לפני {id})
    @GetMapping("/stats")
    public AnimalStatsResponse getAnimalStats() {
        return zooService.getAnimalStats();
    }

    // 3. חיפוש חיות חופשי
    @GetMapping("/search")
    public List<Animal> searchAnimals(@RequestParam(required = false, defaultValue = "") String q) {
        return zooService.searchAnimals(q);
    }

    // 4. קבלת כל החיות בסכנת הכחדה
    @GetMapping("/endangered")
    public List<Animal> getEndangeredAnimals() {
        return zooService.getEndangeredAnimals();
    }

    // 5. קבלת חיות שזקוקות להאכלה
    @GetMapping("/hungry")
    public List<Animal> getHungryAnimals(@RequestParam(defaultValue = "8") int hours) {
        return zooService.getAnimalsNeedingFood(hours);
    }

    // 6. סינון לפי משפחה / מין
    @GetMapping("/species/{species}")
    public List<Animal> getAnimalsBySpecies(@PathVariable Species species) {
        return zooService.getAnimalsBySpecies(species);
    }

    // 7. סינון לפי מצב בריאותי
    @GetMapping("/health/{status}")
    public List<Animal> getAnimalsByHealth(@PathVariable HealthStatus status) {
        return zooService.getAnimalsByHealth(status);
    }

    // 8. סינון לפי סוג תזונה
    @GetMapping("/diet/{dietType}")
    public List<Animal> getAnimalsByDiet(@PathVariable DietType dietType) {
        return zooService.getAnimalsByDiet(dietType);
    }

    // 9. מטא-דאטה עשיר לתמיכה בממשק המשתמש
    @GetMapping("/metadata")
    public Map<String, Object> getMetadata() {
        Map<String, Object> meta = new LinkedHashMap<>();

        List<Map<String, String>> speciesList = new ArrayList<>();
        for (Species s : Species.values()) {
            speciesList.add(Map.of("key", s.name(), "label", s.getHebrewName()));
        }
        meta.put("species", speciesList);

        List<Map<String, String>> subSpeciesList = new ArrayList<>();
        for (SubSpecies ss : SubSpecies.values()) {
            subSpeciesList.add(Map.of("key", ss.name(), "label", ss.getHebrewName(), "species", ss.getSpecies().name()));
        }
        meta.put("subSpecies", subSpeciesList);

        List<Map<String, String>> healthList = new ArrayList<>();
        for (HealthStatus hs : HealthStatus.values()) {
            healthList.add(Map.of("key", hs.name(), "label", hs.getHebrewName()));
        }
        meta.put("healthStatuses", healthList);

        List<Map<String, String>> dietList = new ArrayList<>();
        for (DietType dt : DietType.values()) {
            dietList.add(Map.of("key", dt.name(), "label", dt.getHebrewName()));
        }
        meta.put("dietTypes", dietList);

        List<Map<String, Object>> consList = new ArrayList<>();
        for (ConservationStatus cs : ConservationStatus.values()) {
            consList.add(Map.of("key", cs.name(), "label", cs.getHebrewName(), "endangered", cs.isEndangered()));
        }
        meta.put("conservationStatuses", consList);

        List<Map<String, String>> genderList = new ArrayList<>();
        for (Gender g : Gender.values()) {
            genderList.add(Map.of("key", g.name(), "label", g.getHebrewName()));
        }
        meta.put("genders", genderList);

        return meta;
    }

    // 10. קבלת חיה ספציפית לפי מזהה (מופיע רק פעם אחת עם בדיקת מספר בלבד)
    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<Animal> getAnimalById(@PathVariable Long id) {
        return zooService.getAnimalById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 11. יצירת חיה חדשה
    @PostMapping
    public ResponseEntity<Animal> createAnimal(@RequestBody Animal animal) {
        Animal created = zooService.addAnimal(animal);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // 12. עדכון פרטי חיה
    @PutMapping("/{id:[0-9]+}")
    public ResponseEntity<Animal> updateAnimal(@PathVariable Long id, @RequestBody Animal animal) {
        return zooService.updateAnimal(id, animal)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 13. מחיקת חיה
    @DeleteMapping("/{id:[0-9]+}")
    public ResponseEntity<Void> deleteAnimal(@PathVariable Long id) {
        boolean deleted = zooService.deleteAnimal(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // 14. פעולת האכלת חיה
    @PostMapping("/{id:[0-9]+}/feed")
    public ResponseEntity<Animal> feedAnimal(@PathVariable Long id, @Valid @RequestBody(required = false) FeedingRequest request) {
        FeedingRequest feedingReq = request != null ? request : new FeedingRequest();
        Animal updated = zooService.feedAnimal(id, feedingReq);
        return ResponseEntity.ok(updated);
    }

    // 15. קבלת היסטוריית האכלות של חיה
    @GetMapping("/{id:[0-9]+}/feedings")
    public ResponseEntity<List<FeedingRecord>> getFeedingHistory(@PathVariable Long id) {
        return zooService.getAnimalById(id)
                .map(a -> ResponseEntity.ok(a.getFeedingHistory()))
                .orElse(ResponseEntity.notFound().build());
    }

    // 16. רישום בדיקה רפואית / עדכון מצב בריאותי
    @PostMapping("/{id:[0-9]+}/medical")
    public ResponseEntity<Animal> recordMedicalCheckup(@PathVariable Long id, @Valid @RequestBody MedicalRecordRequest request) {
        Animal updated = zooService.recordMedicalCheckup(id, request);
        return ResponseEntity.ok(updated);
    }

    // 17. קבלת היסטוריה רפואית של חיה
    @GetMapping("/{id:[0-9]+}/medical")
    public ResponseEntity<List<MedicalRecord>> getMedicalHistory(@PathVariable Long id) {
        return zooService.getAnimalById(id)
                .map(a -> ResponseEntity.ok(a.getMedicalHistory()))
                .orElse(ResponseEntity.notFound().build());
    }

    // 18. העברת חיה לבידוד רפואי
    @PostMapping("/{id:[0-9]+}/quarantine")
    public ResponseEntity<Animal> quarantineAnimal(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "בידוד רפואי מונע") String reason,
            @RequestParam(required = false, defaultValue = "וטרינר ראשי") String vetName) {
        Animal updated = zooService.quarantineAnimal(id, reason, vetName);
        return ResponseEntity.ok(updated);
    }

    // 19. העברת חיה לכלוב חדש
    @PutMapping("/{id:[0-9]+}/cage/{cageId:[0-9]+}")
    public ResponseEntity<Animal> transferToCage(@PathVariable Long id, @PathVariable Long cageId) {
        Animal updated = zooService.transferAnimalToCage(id, cageId);
        return ResponseEntity.ok(updated);
    }

    // 20. הוצאת חיה מכלוב (ללא כלוב)
    @DeleteMapping("/{id:[0-9]+}/cage")
    public ResponseEntity<Animal> removeFromCage(@PathVariable Long id) {
        Animal updated = zooService.transferAnimalToCage(id, null);
        return ResponseEntity.ok(updated);
    }
}