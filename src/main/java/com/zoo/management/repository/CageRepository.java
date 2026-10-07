package com.zoo.management.repository;

import com.zoo.management.model.Cage;
import com.zoo.management.model.Species;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CageRepository extends JpaRepository<Cage, Long> {

    List<Cage> findBySpecies(Species species);
}
