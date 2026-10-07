package com.zoo.management.repository;

import com.zoo.management.model.UserRole;
import com.zoo.management.model.ZooTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ZooTaskRepository extends JpaRepository<ZooTask, Long> {
    List<ZooTask> findByStatus(String status);
    List<ZooTask> findByAssignedRole(UserRole role);
    List<ZooTask> findAllByOrderByCreatedAtDesc();
}
