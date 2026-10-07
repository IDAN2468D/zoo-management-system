package com.zoo.management.repository;

import com.zoo.management.model.Species;
import com.zoo.management.model.Veterinarian;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VeterinarianRepository extends JpaRepository<Veterinarian, Long> {

    List<Veterinarian> findBySpecialization(Species specialization);
}
