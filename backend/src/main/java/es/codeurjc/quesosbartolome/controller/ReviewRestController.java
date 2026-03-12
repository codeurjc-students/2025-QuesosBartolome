package es.codeurjc.quesosbartolome.controller;

import java.net.URI;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.codeurjc.quesosbartolome.dto.ReviewDTO;
import es.codeurjc.quesosbartolome.dto.UserDTO;
import es.codeurjc.quesosbartolome.service.ReviewService;
import es.codeurjc.quesosbartolome.service.UserService;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewRestController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;


    @GetMapping("/cheese/{cheeseId}")
    public ResponseEntity<Page<ReviewDTO>> getReviewsByCheeseId(
            @PathVariable Long cheeseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size) {
        
        Page<ReviewDTO> reviews = reviewService.getReviewsByCheeseId(cheeseId, page, size);
        return ResponseEntity.ok(reviews);
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<ReviewDTO>> getReviewsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<ReviewDTO> reviews = reviewService.getReviewsByUserId(userId, page, size);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDTO> getReviewById(@PathVariable Long id) {
        Optional<ReviewDTO> reviewOpt = reviewService.getReviewById(id);
        
        if (reviewOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        return ResponseEntity.ok(reviewOpt.get());
    }


    @PostMapping
    public ResponseEntity<ReviewDTO> createReview(
            @RequestBody Map<String, Object> reviewData,
            Principal principal) {
        
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // Get current user
            Optional<UserDTO> userOpt = userService.findByName(principal.getName());
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Integer rating = (Integer) reviewData.get("rating");
            String comment = (String) reviewData.get("comment");
            Long cheeseId = Long.valueOf(reviewData.get("cheeseId").toString());

            ReviewDTO createdReview = reviewService.createReview(rating, comment, userOpt.get().id(), cheeseId);
            
            return ResponseEntity.created(URI.create("/api/v1/reviews/" + createdReview.id()))
                    .body(createdReview);
                    
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id, Principal principal) {
        
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Verify the review exists and belongs to the user or user is admin
        Optional<ReviewDTO> existingReview = reviewService.getReviewById(id);
        if (existingReview.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Optional<UserDTO> userOpt = userService.findByName(principal.getName());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Allow deletion if user owns the review or is admin
        boolean isOwner = existingReview.get().user().id().equals(userOpt.get().id());
        boolean isAdmin = userOpt.get().rols().contains("ADMIN");

        if (!isOwner && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean deleted = reviewService.deleteReview(id);
        
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        return ResponseEntity.noContent().build();
    }
}
