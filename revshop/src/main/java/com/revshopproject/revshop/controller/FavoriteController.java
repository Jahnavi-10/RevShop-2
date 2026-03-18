package com.revshopproject.revshop.controller;

import com.revshopproject.revshop.service.FavoriteService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * FavoriteController — uses constructor injection (Issue 5 fix).
 */
@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    /**
     * Toggles the favorite status of a product for the logged-in user.
     */
    @PostMapping("/toggle/{productId}")
    public ResponseEntity<?> toggleFavorite(@PathVariable Long productId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Please login to favorite products"));
        }

        try {
            favoriteService.toggleFavorite(userId, productId);
            boolean isFavorited = favoriteService.isProductFavorited(userId, productId);

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "isFavorited", isFavorited,
                "message", isFavorited ? "💖 Added to wishlist" : "💔 Removed from wishlist"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Error updating favorite"));
        }
    }

    /**
     * Check if a product is favorited by the current user.
     */
    @GetMapping("/check/{productId}")
    public ResponseEntity<Boolean> checkFavorite(@PathVariable Long productId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.ok(false);
        return ResponseEntity.ok(favoriteService.isProductFavorited(userId, productId));
    }

    /**
     * Fetch all favorites for the currently logged-in user.
     */
    @GetMapping("/mine")
    public ResponseEntity<?> getMyFavorites(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Please login to view favorites"));
        }
        return ResponseEntity.ok(favoriteService.getFavoritesByUserId(userId));
    }
}