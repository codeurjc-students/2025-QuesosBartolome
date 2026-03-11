package es.codeurjc.quesosbartolome.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.codeurjc.quesosbartolome.model.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByCheeseId(Long cheeseId, Pageable pageable);
    Page<Review> findByUserId(Long userId, Pageable pageable);
}
