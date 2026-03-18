package com.revshopproject.revshop.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.revshopproject.revshop.dto.ReviewRequestDTO;
import com.revshopproject.revshop.entity.Product;
import com.revshopproject.revshop.entity.Review;
import com.revshopproject.revshop.entity.User;
import com.revshopproject.revshop.repository.ProductRepository;
import com.revshopproject.revshop.repository.ReviewRepository;
import com.revshopproject.revshop.service.UserService;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private User testUser;
    private Product testProduct;
    private Review testReview;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);

        testProduct = new Product();
        testProduct.setProductId(100L);

        testReview = new Review();
        testReview.setReviewId(1000L);
        testReview.setUser(testUser);
        testReview.setProduct(testProduct);
        testReview.setRating(5);
        testReview.setCommentText("Great product");
    }

    @Test
    void testAddReview_Success() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(reviewRepository.hasUserPurchasedProduct(testUser.getUserId(), testProduct.getProductId())).thenReturn(true);
        when(reviewRepository.findByUser_UserIdAndProduct_ProductId(testUser.getUserId(), testProduct.getProductId()))
                .thenReturn(Optional.empty());
        when(productRepository.findById(testProduct.getProductId())).thenReturn(Optional.of(testProduct));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        ReviewRequestDTO dto = new ReviewRequestDTO();
        dto.setProductId(testProduct.getProductId());
        dto.setRating(5);
        dto.setComment("Great product");

        Review savedReview = reviewService.addReview(dto);

        assertNotNull(savedReview);
        assertEquals(5, savedReview.getRating());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void testAddReview_NotPurchased_ThrowsException() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(reviewRepository.hasUserPurchasedProduct(testUser.getUserId(), testProduct.getProductId())).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ReviewRequestDTO dto = new ReviewRequestDTO();
            dto.setProductId(testProduct.getProductId());
            dto.setRating(1);
            reviewService.addReview(dto);
        });

        assertTrue(exception.getMessage().contains("Unauthorized"));
    }

    @Test
    void testAddReview_AlreadyExists_ThrowsException() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(reviewRepository.hasUserPurchasedProduct(testUser.getUserId(), testProduct.getProductId())).thenReturn(true);
        when(reviewRepository.findByUser_UserIdAndProduct_ProductId(testUser.getUserId(), testProduct.getProductId()))
                .thenReturn(Optional.of(testReview));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ReviewRequestDTO dto = new ReviewRequestDTO();
            dto.setProductId(testProduct.getProductId());
            dto.setRating(3);
            reviewService.addReview(dto);
        });

        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    void testUpdateReview_Success() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(reviewRepository.findByUser_UserIdAndProduct_ProductId(testUser.getUserId(), testProduct.getProductId()))
                .thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        ReviewRequestDTO dto = new ReviewRequestDTO();
        dto.setProductId(testProduct.getProductId());
        dto.setRating(4);
        dto.setComment("Good product");

        reviewService.updateReview(dto);

        assertEquals(4, testReview.getRating()); // The mock object is modified
        verify(reviewRepository, times(1)).save(testReview);
    }

    @Test
    void testDeleteReview_Success() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(reviewRepository.findByUser_UserIdAndProduct_ProductId(testUser.getUserId(), testProduct.getProductId()))
                .thenReturn(Optional.of(testReview));

        reviewService.deleteReview(testProduct.getProductId());

        verify(reviewRepository, times(1)).delete(testReview);
    }

    @Test
    void testGetReviewStatus() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(reviewRepository.hasUserPurchasedProduct(testUser.getUserId(), testProduct.getProductId())).thenReturn(true);
        when(reviewRepository.findByUser_UserIdAndProduct_ProductId(testUser.getUserId(), testProduct.getProductId()))
                .thenReturn(Optional.of(testReview));

        Map<String, Object> status = reviewService.getReviewStatus(testProduct.getProductId());

        assertTrue((Boolean) status.get("isAuthenticated"));
        assertTrue((Boolean) status.get("hasPurchased"));
        assertTrue((Boolean) status.get("hasReviewed"));
        assertEquals(testReview, status.get("review"));
    }

    @Test
    void testGetReviewsByProduct() {
        when(reviewRepository.findByProduct_ProductId(testProduct.getProductId())).thenReturn(Arrays.asList(testReview));

        List<Review> reviews = reviewService.getReviewsByProduct(testProduct.getProductId());

        assertEquals(1, reviews.size());
        verify(reviewRepository, times(1)).findByProduct_ProductId(testProduct.getProductId());
    }
}
