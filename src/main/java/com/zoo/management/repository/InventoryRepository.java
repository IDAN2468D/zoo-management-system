package com.zoo.management.repository;

import com.zoo.management.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {

    Optional<InventoryItem> findByNameIgnoreCase(String name);

    // שליפת כל הפריטים שנמצאים מתחת לסף המינימלי
    @Query("SELECT i FROM InventoryItem i WHERE i.quantity <= i.minThreshold")
    List<InventoryItem> findLowStockItems();
}