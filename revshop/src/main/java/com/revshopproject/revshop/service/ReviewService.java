package com.revshopproject.revshop.service;

import java.util.List;

import com.revshopproject.revshop.dto.ReviewRequestDTO;
import com.revshopproject.revshop.entity.Review;

public interface ReviewService {
    Review addReview(ReviewRequestDTO dto);
    List<Review> getReviewsByProduct(Long productId);
    Review updateReview(ReviewRequestDTO dto);
    void deleteReview(Long productId);
    java.util.Map<String, Object> getReviewStatus(Long productId);
}