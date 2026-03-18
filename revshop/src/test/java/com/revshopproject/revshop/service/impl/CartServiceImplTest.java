package com.revshopproject.revshop.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.revshopproject.revshop.entity.Cart;
import com.revshopproject.revshop.entity.CartItem;
import com.revshopproject.revshop.entity.Product;
import com.revshopproject.revshop.entity.User;
import com.revshopproject.revshop.repository.CartItemRepository;
import com.revshopproject.revshop.repository.CartRepository;
import com.revshopproject.revshop.repository.ProductRepository;
import com.revshopproject.revshop.service.UserService;

@ExtendWith(MockitoExtension.class)
public class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private CartServiceImpl cartService;

    private User testUser;
    private Cart testCart;
    private Product testProduct;
    private CartItem testCartItem;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setEmail("buyer@example.com");

        testCart = new Cart();
        testCart.setCartId(10L);
        testCart.setUser(testUser);

        testProduct = new Product();
        testProduct.setProductId(100L);
        testProduct.setName("Test Product");
        testProduct.setPrice(new java.math.BigDecimal("10.00"));

        testCartItem = new CartItem();
        testCartItem.setCartItemId(1000L);
        testCartItem.setCart(testCart);
        testCartItem.setProduct(testProduct);
        testCartItem.setQuantity(2);
    }

    @Test
    void testGetCartByUserId_CartExists() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(cartRepository.findByUser_UserId(testUser.getUserId())).thenReturn(Optional.of(testCart));

        Cart cart = cartService.getCart();

        assertNotNull(cart);
        assertEquals(testCart.getCartId(), cart.getCartId());
        verify(cartRepository, times(1)).findByUser_UserId(testUser.getUserId());
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void testGetCartByUserId_CartDoesNotExist() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(cartRepository.findByUser_UserId(testUser.getUserId())).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        Cart cart = cartService.getCart();

        assertNotNull(cart);
        verify(cartRepository, times(1)).findByUser_UserId(testUser.getUserId());
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void testAddItemToCart_NewItem() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(cartRepository.findByUser_UserId(testUser.getUserId())).thenReturn(Optional.of(testCart));
        when(productRepository.findById(testProduct.getProductId())).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByCartAndProduct(testCart, testProduct)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        CartItem item = cartService.addItemToCart(testProduct.getProductId(), 2);

        assertNotNull(item);
        assertEquals(2, item.getQuantity());
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void testAddItemToCart_ExistingItem() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(cartRepository.findByUser_UserId(testUser.getUserId())).thenReturn(Optional.of(testCart));
        when(productRepository.findById(testProduct.getProductId())).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByCartAndProduct(testCart, testProduct)).thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        CartItem item = cartService.addItemToCart(testProduct.getProductId(), 3);

        assertNotNull(item);
        assertEquals(5, item.getQuantity()); // 2 existing + 3 new
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void testUpdateItemQuantity_Success() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(cartItemRepository.findById(testCartItem.getCartItemId())).thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        CartItem updatedItem = cartService.updateItemQuantity(testCartItem.getCartItemId(), 5);

        assertNotNull(updatedItem);
        assertEquals(5, updatedItem.getQuantity());
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void testUpdateItemQuantity_ZeroQuantity_RemovesItem() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(cartItemRepository.findById(testCartItem.getCartItemId())).thenReturn(Optional.of(testCartItem));

        CartItem updatedItem = cartService.updateItemQuantity(testCartItem.getCartItemId(), 0);

        assertNull(updatedItem);
        verify(cartItemRepository, times(1)).delete(testCartItem);
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void testUpdateItemQuantity_UnauthorizedAccess() {
        User otherUser = new User();
        otherUser.setUserId(99L);
        when(userService.getCurrentUser()).thenReturn(otherUser);
        when(cartItemRepository.findById(testCartItem.getCartItemId())).thenReturn(Optional.of(testCartItem));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cartService.updateItemQuantity(testCartItem.getCartItemId(), 5);
        });

        assertTrue(exception.getMessage().contains("Unauthorized"));
    }

    @Test
    void testRemoveItemFromCart_Success() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(cartItemRepository.findById(testCartItem.getCartItemId())).thenReturn(Optional.of(testCartItem));

        cartService.removeItemFromCart(testCartItem.getCartItemId());

        verify(cartItemRepository, times(1)).delete(testCartItem);
    }

    @Test
    void testClearCart_Success() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(cartRepository.findByUser_UserId(testUser.getUserId())).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCart_CartId(testCart.getCartId())).thenReturn(Collections.singletonList(testCartItem));

        cartService.clearCart();

        verify(cartItemRepository, times(1)).deleteAll(anyList());
    }
}
