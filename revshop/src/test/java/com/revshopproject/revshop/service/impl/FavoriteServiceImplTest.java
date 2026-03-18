package com.revshopproject.revshop.service.impl;

import com.revshopproject.revshop.dto.ProductResponseDTO;
import com.revshopproject.revshop.entity.Category;
import com.revshopproject.revshop.entity.Favorite;
import com.revshopproject.revshop.entity.Product;
import com.revshopproject.revshop.entity.User;
import com.revshopproject.revshop.repository.FavoriteRepository;
import com.revshopproject.revshop.repository.ProductRepository;
import com.revshopproject.revshop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FavoriteServiceImplTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private FavoriteServiceImpl favoriteService;

    private User testUser;
    private Product testProduct;
    private Favorite testFavorite;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setEmail("buyer@example.com");

        Category cat = new Category();
        cat.setName("Cat");
        User seller = new User();
        seller.setBusinessName("TestSeller");

        testProduct = new Product();
        testProduct.setProductId(100L);
        testProduct.setName("Test Product");
        testProduct.setCategory(cat);
        testProduct.setSeller(seller);

        testFavorite = new Favorite();
        testFavorite.setFavoriteId(10L);
        testFavorite.setUser(testUser);
        testFavorite.setProduct(testProduct);
    }

    @Test
    void testToggleFavorite_AddFavorite() {
        when(favoriteRepository.findByUser_UserIdAndProduct_ProductId(testUser.getUserId(), testProduct.getProductId()))
                .thenReturn(Optional.empty());
        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(productRepository.findById(testProduct.getProductId())).thenReturn(Optional.of(testProduct));
        when(favoriteRepository.save(any(Favorite.class))).thenReturn(testFavorite);

        favoriteService.toggleFavorite(testUser.getUserId(), testProduct.getProductId());

        verify(favoriteRepository, times(1)).save(any(Favorite.class));
        verify(favoriteRepository, never()).deleteByUser_UserIdAndProduct_ProductId(anyLong(), anyLong());
    }

    @Test
    void testToggleFavorite_RemoveFavorite() {
        when(favoriteRepository.findByUser_UserIdAndProduct_ProductId(testUser.getUserId(), testProduct.getProductId()))
                .thenReturn(Optional.of(testFavorite));
        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(productRepository.findById(testProduct.getProductId())).thenReturn(Optional.of(testProduct));

        favoriteService.toggleFavorite(testUser.getUserId(), testProduct.getProductId());

        verify(favoriteRepository, times(1)).deleteByUser_UserIdAndProduct_ProductId(testUser.getUserId(), testProduct.getProductId());
        verify(favoriteRepository, never()).save(any(Favorite.class));
    }

    @Test
    void testGetFavoritesByUserId() {
        when(favoriteRepository.findByUser_UserId(testUser.getUserId())).thenReturn(Collections.singletonList(testFavorite));

        List<ProductResponseDTO> favorites = favoriteService.getFavoritesByUserId(testUser.getUserId());

        assertNotNull(favorites);
        assertEquals(1, favorites.size());
        assertEquals(testProduct.getName(), favorites.get(0).getName());
        assertTrue(favorites.get(0).isFavorited());
        verify(favoriteRepository, times(1)).findByUser_UserId(testUser.getUserId());
    }

    @Test
    void testIsProductFavorited_True() {
        when(favoriteRepository.findByUser_UserIdAndProduct_ProductId(testUser.getUserId(), testProduct.getProductId()))
                .thenReturn(Optional.of(testFavorite));

        boolean isFavorited = favoriteService.isProductFavorited(testUser.getUserId(), testProduct.getProductId());

        assertTrue(isFavorited);
    }

    @Test
    void testIsProductFavorited_False() {
        when(favoriteRepository.findByUser_UserIdAndProduct_ProductId(testUser.getUserId(), testProduct.getProductId()))
                .thenReturn(Optional.empty());

        boolean isFavorited = favoriteService.isProductFavorited(testUser.getUserId(), testProduct.getProductId());

        assertFalse(isFavorited);
    }

    @Test
    void testClearAllFavorites() {
        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(favoriteRepository.findByUser_UserId(testUser.getUserId())).thenReturn(Collections.singletonList(testFavorite));

        favoriteService.clearAllFavorites(testUser.getUserId());

        verify(favoriteRepository, times(1)).deleteAll(anyList());
    }
}
