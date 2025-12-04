package es.codeurjc.quesosbartolome.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.codeurjc.quesosbartolome.model.Cheese;

@Repository
public interface CheeseRepository extends JpaRepository<Cheese, Long> {

    
}
