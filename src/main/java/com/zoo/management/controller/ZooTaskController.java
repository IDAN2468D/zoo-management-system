package com.zoo.management.controller;

import com.zoo.management.model.ZooTask;
import com.zoo.management.repository.ZooTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class ZooTaskController {

    private static final Logger log = LoggerFactory.getLogger(ZooTaskController.class);
    private final ZooTaskRepository taskRepository;

    public ZooTaskController(ZooTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @GetMapping
    public List<ZooTask> getAllTasks() {
        return taskRepository.findAllByOrderByCreatedAtDesc();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ZooTask> getTaskById(@PathVariable Long id) {
        return taskRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ZooTask> createTask(@RequestBody ZooTask task) {
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        ZooTask saved = taskRepository.save(task);
        log.info("📋 [TASK: משימה חדשה נוצרה] כותרת: '{}' | מיועד לתפקיד: {} | דחיפות: {}",
                saved.getTitle(), saved.getAssignedRole(), saved.getPriority());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ZooTask> updateTaskStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        if (newStatus == null || newStatus.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        return taskRepository.findById(id)
                .map(task -> {
                    task.setStatus(newStatus.toUpperCase());
                    ZooTask updated = taskRepository.save(task);
                    log.info("🔄 [TASK: עדכון סטטוס משימה] ID: {} | סטטוס חדש: {}", id, newStatus);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ZooTask> updateTask(@PathVariable Long id, @RequestBody ZooTask task) {
        return taskRepository.findById(id)
                .map(existing -> {
                    if (task.getTitle() != null) existing.setTitle(task.getTitle());
                    if (task.getDescription() != null) existing.setDescription(task.getDescription());
                    if (task.getAssignedRole() != null) existing.setAssignedRole(task.getAssignedRole());
                    if (task.getAssignedToName() != null) existing.setAssignedToName(task.getAssignedToName());
                    if (task.getAnimalName() != null) existing.setAnimalName(task.getAnimalName());
                    if (task.getPriority() != null) existing.setPriority(task.getPriority());
                    if (task.getStatus() != null) existing.setStatus(task.getStatus());
                    if (task.getDueDate() != null) existing.setDueDate(task.getDueDate());
                    return ResponseEntity.ok(taskRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
            log.info("🗑️ [TASK: מחיקת משימה] ID: {}", id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
