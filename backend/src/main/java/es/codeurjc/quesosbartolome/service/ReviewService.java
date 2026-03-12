package es.codeurjc.quesosbartolome.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import es.codeurjc.quesosbartolome.dto.ReviewDTO;
import es.codeurjc.quesosbartolome.dto.ReviewMapper;
import es.codeurjc.quesosbartolome.model.Cheese;
import es.codeurjc.quesosbartolome.model.Review;
import es.codeurjc.quesosbartolome.model.User;
import es.codeurjc.quesosbartolome.repository.CheeseRepository;
import es.codeurjc.quesosbartolome.repository.ReviewRepository;
import es.codeurjc.quesosbartolome.repository.UserRepository;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private CheeseRepository cheeseRepository;

    @Autowired
    private UserRepository userRepository;

    public Page<ReviewDTO> getReviewsByCheeseId(Long cheeseId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return reviewRepository.findByCheeseId(cheeseId, pageable)
                .map(reviewMapper::toDTO);
    }

    public Page<ReviewDTO> getReviewsByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return reviewRepository.findByUserId(userId, pageable)
                .map(reviewMapper::toDTO);
    }

    public Optional<ReviewDTO> getReviewById(Long id) {
        return reviewRepository.findById(id)
                .map(reviewMapper::toDTO);
    }

    public ReviewDTO createReview(Integer rating, String comment, Long userId, Long cheeseId) {
        
        // Validate rating
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        // Find cheese
        Cheese cheese = cheeseRepository.findById(cheeseId)
                .orElseThrow(() -> new IllegalArgumentException("Cheese not found with id: " + cheeseId));

        // Create review
        Review review = new Review(rating, comment, user, cheese);
        Review savedReview = reviewRepository.save(review);

        return reviewMapper.toDTO(savedReview);
    }

    public boolean deleteReview(Long id) {
        if (!reviewRepository.existsById(id)) {
            return false;
        }
        reviewRepository.deleteById(id);
        return true;
    }
}
