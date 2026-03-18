package com.revshopproject.revshop.service;

import com.revshopproject.revshop.entity.Cart;
import com.revshopproject.revshop.entity.CartItem;

/** All methods operate on the currently authenticated user. No userId parameter needed. */
public interface CartService {
    Cart getCart();
    CartItem addItemToCart(Long productId, Integer quantity);
    void removeItemFromCart(Long cartItemId);
    CartItem updateItemQuantity(Long cartItemId, Integer quantity);
    void clearCart();
}