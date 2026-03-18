package com.revshopproject.revshop.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.revshopproject.revshop.dto.ReviewRequestDTO;
import com.revshopproject.revshop.entity.*;
import com.revshopproject.revshop.repository.*;
import com.revshopproject.revshop.service.ReviewService;

/**
 * ReviewServiceImpl — entity construction moved here from the controller (Issue 6 fix).
 * The controller now passes a ReviewRequestDTO and the service builds the entity.
 */
@Service
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final com.revshopproject.revshop.service.UserService userService;

    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             ProductRepository productRepository,
                             com.revshopproject.revshop.service.UserService userService) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userService = userService;
    }

    @Override
    @Transactional
    public Review addReview(ReviewRequestDTO dto) {
        User user = userService.getCurrentUser();
        Long productId = dto.getProductId();

        // 1. Purchase Check
        if (!reviewRepository.hasUserPurchasedProduct(user.getUserId(), productId)) {
            throw new RuntimeException("Unauthorized: You must receive this product before reviewing it.");
        }

        // 2. Prevent duplicate reviews
        if (reviewRepository.findByUser_UserIdAndProduct_ProductId(user.getUserId(), productId).isPresent()) {
            throw new RuntimeException("Review already exists. Use the update endpoint instead.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Build entity here in the service layer
        Review review = new Review();
        review.setRating(dto.getRating());
        review.setCommentText(dto.getComment());
        review.setUser(user);
        review.setProduct(product);
        return reviewRepository.save(review);
    }

    @Override
    @Transactional
    public Review updateReview(ReviewRequestDTO dto) {
        User user = userService.getCurrentUser();
        Review existingReview = reviewRepository.findByUser_UserIdAndProduct_ProductId(user.getUserId(), dto.getProductId())
                .orElseThrow(() -> new RuntimeException("No existing review found to update."));

        existingReview.setRating(dto.getRating());
        existingReview.setCommentText(dto.getComment());
        return reviewRepository.save(existingReview);
    }

    @Override
    @Transactional
    public void deleteReview(Long productId) {
        User user = userService.getCurrentUser();
        Review existingReview = reviewRepository.findByUser_UserIdAndProduct_ProductId(user.getUserId(), productId)
                .orElseThrow(() -> new RuntimeException("No existing review found to delete."));
        reviewRepository.delete(existingReview);
    }

    @Override
    public java.util.Map<String, Object> getReviewStatus(Long productId) {
        java.util.Map<String, Object> status = new java.util.HashMap<>();
        try {
            User user = userService.getCurrentUser();
            boolean hasPurchased = reviewRepository.hasUserPurchasedProduct(user.getUserId(), productId);
            java.util.Optional<Review> existingReview = reviewRepository.findByUser_UserIdAndProduct_ProductId(user.getUserId(), productId);

            status.put("isAuthenticated", true);
            status.put("hasPurchased", hasPurchased);
            status.put("hasReviewed", existingReview.isPresent());
            status.put("review", existingReview.orElse(null));
        } catch (Exception e) {
            status.put("isAuthenticated", false);
            status.put("hasPurchased", false);
            status.put("hasReviewed", false);
        }
        return status;
    }

    @Override
    public List<Review> getReviewsByProduct(Long productId) {
        return reviewRepository.findByProduct_ProductId(productId);
    }
}