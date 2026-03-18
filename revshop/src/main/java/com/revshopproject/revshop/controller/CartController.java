package com.revshopproject.revshop.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.revshopproject.revshop.entity.Cart;
import com.revshopproject.revshop.entity.CartItem;
import com.revshopproject.revshop.service.CartService;

/**
 * CartController — no userId parameters (Issue 7 fix).
 * All methods rely on the authenticated user resolved by the service layer.
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // GET: /api/cart
    @GetMapping
    public ResponseEntity<com.revshopproject.revshop.dto.CartResponseDTO> getCart() {
        Cart cart = cartService.getCart();
        return ResponseEntity.ok(com.revshopproject.revshop.dto.CartResponseDTO.fromEntity(cart));
    }

    // POST: /api/cart/add?productId=5&quantity=2
    @PostMapping("/add")
    public ResponseEntity<com.revshopproject.revshop.dto.CartItemResponseDTO> addToCart(
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        CartItem cartItem = cartService.addItemToCart(productId, quantity);
        return ResponseEntity.ok(com.revshopproject.revshop.dto.CartItemResponseDTO.fromEntity(cartItem));
    }

    // DELETE: /api/cart/item/{cartItemId}
    @DeleteMapping("/item/{cartItemId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long cartItemId) {
        cartService.removeItemFromCart(cartItemId);
        return ResponseEntity.noContent().build();
    }

    // PATCH: /api/cart/item/{cartItemId}?quantity=5
    @PatchMapping("/item/{cartItemId}")
    public ResponseEntity<com.revshopproject.revshop.dto.CartItemResponseDTO> updateItemQuantity(
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity) {
        CartItem updatedItem = cartService.updateItemQuantity(cartItemId, quantity);
        if (updatedItem == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(com.revshopproject.revshop.dto.CartItemResponseDTO.fromEntity(updatedItem));
    }

    // DELETE: /api/cart/clear
    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart();
        return ResponseEntity.noContent().build();
    }
}