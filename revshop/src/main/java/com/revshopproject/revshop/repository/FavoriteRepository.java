package com.revshopproject.revshop.repository;

import com.revshopproject.revshop.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    // Get all favorites for a specific user
    List<Favorite> findByUser_UserId(Long userId);

    // Check if a specific product is already favorited by the user
    Optional<Favorite> findByUser_UserIdAndProduct_ProductId(Long userId, Long productId);

    // Remove a favorite
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteByUser_UserIdAndProduct_ProductId(Long userId, Long productId);
}