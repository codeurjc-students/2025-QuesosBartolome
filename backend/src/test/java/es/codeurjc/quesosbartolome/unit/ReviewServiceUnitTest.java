package es.codeurjc.quesosbartolome.unit;

import es.codeurjc.quesosbartolome.dto.ReviewDTO;
import es.codeurjc.quesosbartolome.dto.ReviewMapper;
import es.codeurjc.quesosbartolome.model.Cheese;
import es.codeurjc.quesosbartolome.model.Review;
import es.codeurjc.quesosbartolome.model.User;
import es.codeurjc.quesosbartolome.repository.CheeseRepository;
import es.codeurjc.quesosbartolome.repository.ReviewRepository;
import es.codeurjc.quesosbartolome.repository.UserRepository;
import es.codeurjc.quesosbartolome.service.ReviewService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceUnitTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private CheeseRepository cheeseRepository;

    @Mock
    private UserRepository userRepository;

    @Spy
    private ReviewMapper reviewMapper = Mappers.getMapper(ReviewMapper.class);

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void getReviewsByCheeseIdReturnsMappedPage() {
        Review r1 = new Review();
        r1.setId(1L);
        Review r2 = new Review();
        r2.setId(2L);

        Page<Review> page = new PageImpl<>(List.of(r1, r2));
        when(reviewRepository.findByCheeseId(eq(10L), any(Pageable.class))).thenReturn(page);

        Page<ReviewDTO> result = reviewService.getReviewsByCheeseId(10L, 0, 5);

        assertThat(result).hasSize(2);
        assertThat(result.map(ReviewDTO::id).toList()).contains(1L, 2L);
        verify(reviewRepository).findByCheeseId(eq(10L), any(Pageable.class));
    }

    @Test
    void getReviewsByUserIdReturnsMappedPage() {
        Review r1 = new Review();
        r1.setId(3L);

        Page<Review> page = new PageImpl<>(List.of(r1));
        when(reviewRepository.findByUserId(eq(5L), any(Pageable.class))).thenReturn(page);

        Page<ReviewDTO> result = reviewService.getReviewsByUserId(5L, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).id()).isEqualTo(3L);
        verify(reviewRepository).findByUserId(eq(5L), any(Pageable.class));
    }

    @Test
    void getReviewByIdReturnsEmptyWhenNotFound() {
        when(reviewRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<ReviewDTO> result = reviewService.getReviewById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void getReviewByIdReturnsDTOWhenFound() {
        Review review = new Review();
        review.setId(7L);
        when(reviewRepository.findById(7L)).thenReturn(Optional.of(review));

        Optional<ReviewDTO> result = reviewService.getReviewById(7L);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(7L);
    }

    @Test
    void createReviewThrowsWhenRatingInvalid() {
        assertThatThrownBy(() -> reviewService.createReview(0, "bad", 1L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rating must be between 1 and 5");

        assertThatThrownBy(() -> reviewService.createReview(6, "bad", 1L, 1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createReviewThrowsWhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.createReview(5, "ok", 1L, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void createReviewThrowsWhenCheeseNotFound() {
        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cheeseRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.createReview(4, "ok", 1L, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cheese not found");
    }

    @Test
    void createReviewSavesAndReturnsDTO() {
        User user = new User();
        user.setId(1L);

        Cheese cheese = new Cheese();
        cheese.setId(10L);

        Review saved = new Review();
        saved.setId(100L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cheeseRepository.findById(10L)).thenReturn(Optional.of(cheese));
        when(reviewRepository.save(any(Review.class))).thenReturn(saved);

        ReviewDTO dto = reviewService.createReview(5, "Muy bueno", 1L, 10L);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(100L);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void deleteReviewReturnsFalseWhenNotExists() {
        when(reviewRepository.existsById(50L)).thenReturn(false);

        boolean result = reviewService.deleteReview(50L);

        assertThat(result).isFalse();
        verify(reviewRepository, never()).deleteById(any());
    }

    @Test
    void deleteReviewDeletesWhenExists() {
        when(reviewRepository.existsById(20L)).thenReturn(true);

        boolean result = reviewService.deleteReview(20L);

        assertThat(result).isTrue();
        verify(reviewRepository).deleteById(20L);
    }
}
