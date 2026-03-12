package es.codeurjc.quesosbartolome.integration;

import es.codeurjc.quesosbartolome.dto.ReviewDTO;
import es.codeurjc.quesosbartolome.model.Cheese;
import es.codeurjc.quesosbartolome.model.Review;
import es.codeurjc.quesosbartolome.model.User;
import es.codeurjc.quesosbartolome.repository.CheeseRepository;
import es.codeurjc.quesosbartolome.repository.ReviewRepository;
import es.codeurjc.quesosbartolome.repository.UserRepository;
import es.codeurjc.quesosbartolome.service.ReviewService;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ReviewServiceIntegrationTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CheeseRepository cheeseRepository;

    private User user;
    private Cheese cheese;

    @BeforeEach
    void setup() {
        reviewRepository.deleteAll();
        userRepository.deleteAll();
        cheeseRepository.deleteAll();

        // Create user
        user = new User();
        user.setName("juan");
        userRepository.save(user);

        // Create cheese
        cheese = new Cheese(null, "Manchego", 12.0, "desc", "tipo", "2024-01-01", "2025-01-01");
        cheeseRepository.save(cheese);
    }

    @AfterAll
    static void cleanup(
            @Autowired ReviewRepository reviewRepository,
            @Autowired UserRepository userRepository,
            @Autowired CheeseRepository cheeseRepository) {

        reviewRepository.deleteAll();
        userRepository.deleteAll();
        cheeseRepository.deleteAll();
    }

    @Test
    void shouldReturnReviewsByCheese() {
        Review r1 = new Review(5, "Muy bueno", user, cheese);
        Review r2 = new Review(4, "Bueno", user, cheese);
        reviewRepository.saveAll(List.of(r1, r2));

        Page<ReviewDTO> page = reviewService.getReviewsByCheeseId(cheese.getId(), 0, 10);

        assertThat(page).hasSize(2);
        assertThat(page.getContent().get(0).rating()).isIn(4, 5);
    }

    @Test
    void shouldReturnReviewsByUser() {
        Review r1 = new Review(3, "ok", user, cheese);
        reviewRepository.save(r1);

        Page<ReviewDTO> page = reviewService.getReviewsByUserId(user.getId(), 0, 10);

        assertThat(page).hasSize(1);
        assertThat(page.getContent().get(0).comment()).isEqualTo("ok");
    }

    @Test
    void shouldReturnReviewById() {
        Review review = new Review(5, "Excelente", user, cheese);
        reviewRepository.save(review);

        Optional<ReviewDTO> result = reviewService.getReviewById(review.getId());

        assertThat(result).isPresent();
        assertThat(result.get().comment()).isEqualTo("Excelente");
    }

    @Test
    void shouldReturnEmptyWhenReviewNotFound() {
        Optional<ReviewDTO> result = reviewService.getReviewById(999L);
        assertThat(result).isEmpty();
    }

    @Test
    void createReviewShouldPersistAndReturnDTO() {
        ReviewDTO dto = reviewService.createReview(5, "Perfecto", user.getId(), cheese.getId());

        assertThat(dto).isNotNull();
        assertThat(dto.rating()).isEqualTo(5);

        assertThat(reviewRepository.count()).isEqualTo(1);
    }

    @Test
    void createReviewShouldFailWhenUserNotFound() {
        assertThatThrownBy(() -> reviewService.createReview(4, "ok", 999L, cheese.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void createReviewShouldFailWhenCheeseNotFound() {
        assertThatThrownBy(() -> reviewService.createReview(4, "ok", user.getId(), 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cheese not found");
    }

    @Test
    void createReviewShouldFailWhenRatingInvalid() {
        assertThatThrownBy(() -> reviewService.createReview(0, "mal", user.getId(), cheese.getId()))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> reviewService.createReview(6, "mal", user.getId(), cheese.getId()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deleteReviewShouldReturnTrueWhenExists() {
        Review review = new Review(4, "bien", user, cheese);
        reviewRepository.save(review);

        boolean result = reviewService.deleteReview(review.getId());

        assertThat(result).isTrue();
        assertThat(reviewRepository.existsById(review.getId())).isFalse();
    }

    @Test
    void deleteReviewShouldReturnFalseWhenNotExists() {
        boolean result = reviewService.deleteReview(999L);
        assertThat(result).isFalse();
    }
}
