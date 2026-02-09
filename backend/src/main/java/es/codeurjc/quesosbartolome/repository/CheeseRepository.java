package es.codeurjc.quesosbartolome.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.codeurjc.quesosbartolome.model.Cheese;
import java.util.Optional;

@Repository
public interface CheeseRepository extends JpaRepository<Cheese, Long> {
    boolean existsByName(String name);
    Optional<Cheese> findByName(String name);
}
