package com.zoo.management.repository;

import com.zoo.management.model.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    List<MedicalRecord> findByAnimalIdOrderByTimestampDesc(Long animalId);
}
