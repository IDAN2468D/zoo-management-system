package com.zoo.management.controller;

import com.zoo.management.model.*;
import com.zoo.management.repository.*;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "*")
public class AlertController {

    private final AnimalRepository animalRepository;
    private final CageRepository cageRepository;
    private final ZooTaskRepository taskRepository;
    private final InventoryRepository inventoryRepository;

    public AlertController(AnimalRepository animalRepository, CageRepository cageRepository, ZooTaskRepository taskRepository, InventoryRepository inventoryRepository) {
        this.animalRepository = animalRepository;
        this.cageRepository = cageRepository;
        this.taskRepository = taskRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @GetMapping
    public Map<String, Object> getActiveAlerts() {
        List<Map<String, Object>> alerts = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        List<Animal> animals = animalRepository.findAll();
        List<Cage> cages = cageRepository.findAll();
        List<ZooTask> tasks = taskRepository.findAll();

        int criticalCount = 0;
        int warningCount = 0;
        int infoCount = 0;

        // 1. התראות בריאות חמורות (בידוד, חולה, קריטי)
        for (Animal a : animals) {
            HealthStatus status = a.getHealthStatus();
            if (status == HealthStatus.QUARANTINED) {
                criticalCount++;
                Map<String, Object> alert = new HashMap<>();
                alert.put("id", "quarantine-" + a.getId());
                alert.put("type", "CRITICAL");
                alert.put("category", "HEALTH");
                alert.put("icon", "🚨");
                alert.put("title", a.getName() + " נמצא/ת בבידוד רפואי קפדני");
                alert.put("message", "מין: " + (a.getSubSpecies() != null ? a.getSubSpecies().getHebrewName() : a.getSpecies().getHebrewName()) + " | דורש מעקב וטרינרי צמוד");
                alert.put("animalId", a.getId());
                alert.put("actionType", "MEDICAL");
                alert.put("actionText", "צפה בתיק רפואי");
                alerts.add(alert);
            } else if (status == HealthStatus.SICK || status == HealthStatus.INJURED) {
                warningCount++;
                Map<String, Object> alert = new HashMap<>();
                alert.put("id", "health-" + a.getId());
                alert.put("type", "WARNING");
                alert.put("category", "HEALTH");
                alert.put("icon", "🩺");
                alert.put("title", a.getName() + " נמצא/ת בטיפול רפואי פעיל");
                alert.put("message", "סטטוס: " + status.getHebrewName() + " | מומלץ לבדוק מתן תרופות ומעקב");
                alert.put("animalId", a.getId());
                alert.put("actionType", "MEDICAL");
                alert.put("actionText", "בדיקה רפואית");
                alerts.add(alert);
            } else if (status == HealthStatus.UNDER_OBSERVATION) {
                infoCount++;
                Map<String, Object> alert = new HashMap<>();
                alert.put("id", "obs-" + a.getId());
                alert.put("type", "INFO");
                alert.put("category", "HEALTH");
                alert.put("icon", "👁️");
                alert.put("title", a.getName() + " בהשגחה שגרתית");
                alert.put("message", "מעקב תקופתי רגוע ללא סימני מצוקה חריגים");
                alert.put("animalId", a.getId());
                alert.put("actionType", "VIEW");
                alert.put("actionText", "צפה בפרטים");
                alerts.add(alert);
            }
        }

        // 2. התראות האכלה (חיות שלא הואכלו מעל 8 שעות)
        for (Animal a : animals) {
            LocalDateTime lastFed = a.getLastFedTime();
            long hoursAgo = (lastFed != null) ? Duration.between(lastFed, now).toHours() : 99;

            if (hoursAgo >= 12) {
                criticalCount++;
                Map<String, Object> alert = new HashMap<>();
                alert.put("id", "hungry-crit-" + a.getId());
                alert.put("type", "CRITICAL");
                alert.put("category", "FEEDING");
                alert.put("icon", "🥩");
                alert.put("title", a.getName() + " לא הואכל/ה מעל " + hoursAgo + " שעות!");
                alert.put("message", "מזון מועדף: " + (a.getFavoriteFood() != null ? a.getFavoriteFood() : "מזון שגרתי") + " | לוח זמנים: " + (a.getFeedingSchedule() != null ? a.getFeedingSchedule() : "רגיל"));
                alert.put("animalId", a.getId());
                alert.put("actionType", "FEED");
                alert.put("actionText", "האכל עכשיו");
                alerts.add(alert);
            } else if (hoursAgo >= 8) {
                warningCount++;
                Map<String, Object> alert = new HashMap<>();
                alert.put("id", "hungry-warn-" + a.getId());
                alert.put("type", "WARNING");
                alert.put("category", "FEEDING");
                alert.put("icon", "🍖");
                alert.put("title", "זמן האכלה הגיע עבור " + a.getName());
                alert.put("message", "עברו כ-" + hoursAgo + " שעות מהאכלה אחרונה");
                alert.put("animalId", a.getId());
                alert.put("actionType", "FEED");
                alert.put("actionText", "בצע האכלה");
                alerts.add(alert);
            }
        }

        // 3. התראות תפוסת כלובים
        Map<Long, Long> animalsPerCage = new HashMap<>();
        for (Animal a : animals) {
            if (a.getCage() != null && a.getCage().getId() != null) {
                animalsPerCage.put(a.getCage().getId(), animalsPerCage.getOrDefault(a.getCage().getId(), 0L) + 1);
            }
        }

        for (Cage c : cages) {
            long count = animalsPerCage.getOrDefault(c.getId(), 0L);
            int capacity = c.getCapacity() != null ? c.getCapacity() : 6;
            if (count > capacity) {
                criticalCount++;
                Map<String, Object> alert = new HashMap<>();
                alert.put("id", "cage-overflow-" + c.getId());
                alert.put("type", "CRITICAL");
                alert.put("category", "CAGE");
                alert.put("icon", "⚠️");
                alert.put("title", "תפוסת יתר ב" + c.getName() + "!");
                alert.put("message", "בכלוב שוהות " + count + " חיות מתוך קיבולת מותרת של " + capacity);
                alert.put("cageId", c.getId());
                alert.put("actionType", "CAGE");
                alert.put("actionText", "נהל כלוב");
                alerts.add(alert);
            }
        }

        // 4. משימות דחופות שממתינות לביצוע
        for (ZooTask t : tasks) {
            if ("URGENT".equalsIgnoreCase(t.getPriority()) && !"COMPLETED".equalsIgnoreCase(t.getStatus())) {
                warningCount++;
                Map<String, Object> alert = new HashMap<>();
                alert.put("id", "task-urgent-" + t.getId());
                alert.put("type", "WARNING");
                alert.put("category", "TASK");
                alert.put("icon", "⚡");
                alert.put("title", "משימה דחופה ממתינה: " + t.getTitle());
                alert.put("message", "מיועדת ל: " + t.getAssignedRole() + (t.getAssignedToName() != null ? " (" + t.getAssignedToName() + ")" : "") + " | יעד: " + (t.getDueDate() != null ? t.getDueDate() : "היום"));
                alert.put("taskId", t.getId());
                alert.put("actionType", "TASK");
                alert.put("actionText", "טפל במשימה");
                alerts.add(alert);
            }
        }

        // מיון: קריטי ראשון, אח"כ אזהרה, אח"כ מידע
        alerts.sort((a, b) -> {
            String typeA = (String) a.get("type");
            String typeB = (String) b.get("type");
            int weightA = "CRITICAL".equals(typeA) ? 3 : "WARNING".equals(typeA) ? 2 : 1;
            int weightB = "CRITICAL".equals(typeB) ? 3 : "WARNING".equals(typeB) ? 2 : 1;
            return Integer.compare(weightB, weightA);
        });

        // בתוך מתודת getActiveAlerts():
        List<InventoryItem> lowStockItems = inventoryRepository.findLowStockItems();
        for (InventoryItem item : lowStockItems) {
            warningCount++;
            Map<String, Object> alert = new HashMap<>();
            alert.put("id", "inv-" + item.getId());
            alert.put("type", "WARNING");
            alert.put("category", "INVENTORY");
            alert.put("icon", "📦");
            alert.put("title", "מלאי נמוך: " + item.getName());
            alert.put("message", "נותרו " + item.getQuantity() + " " + item.getUnit() + " בלבד (סף מינימום: " + item.getMinThreshold() + ")");
            alert.put("actionType", "RESTOCK");
            alert.put("actionText", "חידוש מלאי");
            alerts.add(alert);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalAlerts", alerts.size());
        response.put("criticalCount", criticalCount);
        response.put("warningCount", warningCount);
        response.put("infoCount", infoCount);
        response.put("alerts", alerts);
        return response;
    }
}
