package com.zoo.management.controller;

import com.zoo.management.model.InventoryItem;
import com.zoo.management.service.ZooService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*")
public class InventoryController {

    private final ZooService zooService;

    public InventoryController(ZooService zooService) {
        this.zooService = zooService;
    }

    @GetMapping
    public ResponseEntity<List<InventoryItem>> getInventory() {
        return ResponseEntity.ok(zooService.getAllInventory());
    }

    @PostMapping
    public ResponseEntity<InventoryItem> createItem(@RequestBody InventoryItem item) {
        return ResponseEntity.ok(zooService.addOrUpdateInventory(item));
    }

    // ✏️ עריכת פריט מלאי קיים לפי מזהה (ID)
    @PutMapping("/{id}")
    public ResponseEntity<InventoryItem> updateItem(@PathVariable Long id, @RequestBody InventoryItem item) {
        return zooService.updateInventoryItem(id, item)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 🗑️ מחיקת פריט מלאי לפי מזהה (ID)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        boolean deleted = zooService.deleteInventoryItem(id);
        if (deleted) {
            return ResponseEntity.noContent().build(); // 204 No Content
        }
        return ResponseEntity.notFound().build(); // 404 Not Found
    }

    @PostMapping("/{id}/restock")
    public ResponseEntity<InventoryItem> restock(@PathVariable Long id, @RequestBody Map<String, Double> payload) {
        Double amount = payload.getOrDefault("amount", 0.0);
        return ResponseEntity.ok(zooService.restockItem(id, amount));
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<InventoryItem>> createBulkItems(@RequestBody List<InventoryItem> items) {
        for (InventoryItem item : items) {
            zooService.addOrUpdateInventory(item);
        }
        return ResponseEntity.ok(zooService.getAllInventory());
    }
}