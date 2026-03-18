package com.revshopproject.revshop.service.impl;

import com.revshopproject.revshop.dto.ProductResponseDTO;
import com.revshopproject.revshop.entity.Favorite;
import com.revshopproject.revshop.entity.Product;
import com.revshopproject.revshop.entity.User;
import com.revshopproject.revshop.repository.FavoriteRepository;
import com.revshopproject.revshop.repository.ProductRepository;
import com.revshopproject.revshop.repository.UserRepository;
import com.revshopproject.revshop.service.FavoriteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    private static final Logger logger = LoggerFactory.getLogger(FavoriteServiceImpl.class);

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    @Transactional
    public void toggleFavorite(Long userId, Long productId) {
        Optional<Favorite> existing = favoriteRepository.findByUser_UserIdAndProduct_ProductId(userId, productId);
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));

        if (existing.isPresent()) {
            logger.info("User {} removed product '{}' from favorites.", user.getEmail(), product.getName());
            favoriteRepository.deleteByUser_UserIdAndProduct_ProductId(userId, productId);
        } else {
            Favorite favorite = new Favorite();
            favorite.setUser(user);
            favorite.setProduct(product);
            favoriteRepository.save(favorite);
            logger.info("User {} added product '{}' to favorites.", user.getEmail(), product.getName());
        }
    }

    @Override
    public List<ProductResponseDTO> getFavoritesByUserId(Long userId) {
        List<Favorite> favorites = favoriteRepository.findByUser_UserId(userId);
        return favorites.stream()
                .map(f -> convertToProductResponseDTO(f.getProduct()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isProductFavorited(Long userId, Long productId) {
        return favoriteRepository.findByUser_UserIdAndProduct_ProductId(userId, productId).isPresent();
    }

    @Override
    @Transactional
    public void clearAllFavorites(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        String userEmail = (user != null) ? user.getEmail() : userId.toString();
        
        logger.info("User {} cleared all favorites.", userEmail);
        List<Favorite> userFavorites = favoriteRepository.findByUser_UserId(userId);
        favoriteRepository.deleteAll(userFavorites);
    }

    private ProductResponseDTO convertToProductResponseDTO(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setMrp(product.getMrp());
        dto.setDescription(product.getDescription());
        
        if (product.getCategory() != null) {
            dto.setCategoryName(product.getCategory().getName());
        }
        if (product.getSeller() != null) {
            dto.setSellerBusinessName(product.getSeller().getBusinessName());
        }
        
        dto.setPrimaryImageUrl(product.getPrimaryImageUrl());
        dto.setFavorited(true);
        return dto;
    }
}