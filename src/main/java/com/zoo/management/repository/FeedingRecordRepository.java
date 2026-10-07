package com.zoo.management.repository;

import com.zoo.management.model.FeedingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedingRecordRepository extends JpaRepository<FeedingRecord, Long> {

    List<FeedingRecord> findByAnimalIdOrderByTimestampDesc(Long animalId);
}
