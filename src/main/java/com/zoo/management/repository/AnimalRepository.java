package com.zoo.management.repository;

import com.zoo.management.model.Animal;
import com.zoo.management.model.DietType;
import com.zoo.management.model.HealthStatus;
import com.zoo.management.model.Species;
import com.zoo.management.model.SubSpecies;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnimalRepository extends JpaRepository<Animal, Long> {

    List<Animal> findBySpecies(Species species);

    List<Animal> findBySubSpecies(SubSpecies subSpecies);

    List<Animal> findByHealthStatus(HealthStatus healthStatus);

    List<Animal> findByDietType(DietType dietType);

    List<Animal> findByCageId(Long cageId);

    @Query("SELECT a FROM Animal a WHERE " +
           "LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.microchipId) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.originCountry) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.notes) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Animal> searchAnimals(@Param("query") String query);
}
