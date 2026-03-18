package com.revshopproject.revshop.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.revshopproject.revshop.entity.*;
import com.revshopproject.revshop.repository.*;
import com.revshopproject.revshop.service.CartService;
import com.revshopproject.revshop.service.UserService;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserService userService;

    public CartServiceImpl(CartRepository cartRepository, 
                           CartItemRepository cartItemRepository, 
                           ProductRepository productRepository,
                           UserService userService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userService = userService;
    }

    @Override
    public Cart getCart() {
        User currentUser = userService.getCurrentUser();
        return cartRepository.findByUser_UserId(currentUser.getUserId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(currentUser);
                    return cartRepository.save(newCart);
                });
    }

    @Override
    @Transactional
    public CartItem addItemToCart(Long productId, Integer quantity) {
        User currentUser = userService.getCurrentUser();
        Cart cart = getCart();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        return cartItemRepository.findByCartAndProduct(cart, product)
                .map(existingItem -> {
                    existingItem.setQuantity(existingItem.getQuantity() + quantity);
                    logger.info("Updated quantity of product {} in user {}'s cart.", product.getName(), currentUser.getEmail());
                    return cartItemRepository.save(existingItem);
                })
                .orElseGet(() -> {
                    logger.info("Added new product {} to user {}'s cart.", product.getName(), currentUser.getEmail());
                    return cartItemRepository.save(new CartItem(null, cart, product, quantity));
                });
    }

    @Override
    @Transactional
    public CartItem updateItemQuantity(Long cartItemId, Integer quantity) {
        User currentUser = userService.getCurrentUser();
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("CartItem not found"));
        
        // Ownership Check
        if (!item.getCart().getUser().getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("Unauthorized: This item does not belong to your cart.");
        }
        
        if (quantity <= 0) {
            logger.info("Removing product {} from user {}'s cart (quantity updated to 0).", item.getProduct().getName(), currentUser.getEmail());
            cartItemRepository.delete(item);
            return null;
        } else {
            item.setQuantity(quantity);
            return cartItemRepository.save(item);
        }
    }

    @Override
    public void removeItemFromCart(Long cartItemId) {
        User currentUser = userService.getCurrentUser();
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("CartItem not found"));
        
        // Ownership Check
        if (!item.getCart().getUser().getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("Unauthorized: This item does not belong to your cart.");
        }
        
        logger.info("Removing product {} from user {}'s cart.", item.getProduct().getName(), currentUser.getEmail());
        cartItemRepository.delete(item);
    }

    @Override
    @Transactional
    public void clearCart() {
        User currentUser = userService.getCurrentUser();
        Cart cart = getCart();
        List<CartItem> items = cartItemRepository.findByCart_CartId(cart.getCartId());
        logger.info("Clearing all {} items from user {}'s cart.", items.size(), currentUser.getEmail());
        cartItemRepository.deleteAll(items);
    }
}