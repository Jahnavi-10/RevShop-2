package com.revshopproject.revshop.service.impl;

import com.revshopproject.revshop.dto.OrderRequestDTO;
import com.revshopproject.revshop.dto.OrderResponseDTO;
import com.revshopproject.revshop.entity.*;
import com.revshopproject.revshop.repository.*;
import com.revshopproject.revshop.service.NotificationService;
import com.revshopproject.revshop.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserService userService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User testBuyer;
    private User testSeller;
    private Product testProduct;
    private Cart testCart;
    private CartItem testCartItem;
    private Order testOrder;
    private OrderRequestDTO orderRequestDTO;

    @BeforeEach
    void setUp() {
        testBuyer = new User();
        testBuyer.setUserId(1L);
        testBuyer.setRole("BUYER");

        testSeller = new User();
        testSeller.setUserId(2L);
        testSeller.setRole("SELLER");

        testProduct = new Product();
        testProduct.setProductId(100L);
        testProduct.setName("Test Product");
        testProduct.setPrice(new BigDecimal("50.00"));
        testProduct.setStock(10);
        testProduct.setSeller(testSeller);

        testCart = new Cart();
        testCart.setCartId(10L);
        testCart.setUser(testBuyer);

        testCartItem = new CartItem();
        testCartItem.setCartItemId(100L);
        testCartItem.setCart(testCart);
        testCartItem.setProduct(testProduct);
        testCartItem.setQuantity(2); // Total = 100

        testOrder = new Order();
        testOrder.setOrderId(500L);
        testOrder.setUser(testBuyer);
        testOrder.setStatus("PENDING");

        orderRequestDTO = new OrderRequestDTO();
        orderRequestDTO.setPaymentMethod("CREDIT_CARD");
        orderRequestDTO.setShippingAddress("123 Test St");
    }

    @Test
    void testPlaceOrder_Success() {
        when(userService.getCurrentUser()).thenReturn(testBuyer);
        when(cartRepository.findByUser_UserId(testBuyer.getUserId())).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCart_CartId(testCart.getCartId())).thenReturn(Arrays.asList(testCartItem));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(new OrderItem());
        
        OrderResponseDTO response = orderService.placeOrder(orderRequestDTO);

        assertNotNull(response);
        assertEquals(500L, response.getOrderId());
        verify(orderRepository, atLeastOnce()).save(any(Order.class));
        verify(orderItemRepository, times(1)).save(any(OrderItem.class));
        verify(cartItemRepository, times(1)).deleteAll(anyList());
        verify(notificationService, atLeastOnce()).sendNotification(any(User.class), anyString());
    }

    @Test
    void testPlaceOrder_EmptyCart() {
        when(userService.getCurrentUser()).thenReturn(testBuyer);
        when(cartRepository.findByUser_UserId(testBuyer.getUserId())).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCart_CartId(testCart.getCartId())).thenReturn(Arrays.asList());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.placeOrder(orderRequestDTO);
        });

        assertTrue(exception.getMessage().contains("Cart is empty"));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testPlaceOrder_InsufficientStock() {
        testProduct.setStock(1); // Cart needs 2
        when(userService.getCurrentUser()).thenReturn(testBuyer);
        when(cartRepository.findByUser_UserId(testBuyer.getUserId())).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCart_CartId(testCart.getCartId())).thenReturn(Arrays.asList(testCartItem));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.placeOrder(orderRequestDTO);
        });

        assertTrue(exception.getMessage().contains("out of stock"));
    }
    
    @Test
    void testAcceptOrder_Success() {
        when(userService.getCurrentUser()).thenReturn(testSeller);
        when(orderRepository.findById(testOrder.getOrderId())).thenReturn(Optional.of(testOrder));
        
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(testProduct);
        orderItem.setQuantity(2);
        when(orderItemRepository.findByOrder_OrderId(testOrder.getOrderId())).thenReturn(Arrays.asList(orderItem));
        
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        orderService.acceptOrder(testOrder.getOrderId());

        assertEquals("ACCEPTED", testOrder.getStatus());
        assertEquals(8, testProduct.getStock()); // 10 initial - 2
        verify(productRepository, times(1)).save(testProduct);
        verify(orderRepository, times(1)).save(testOrder);
    }
    
    @Test
    void testUpdateOrderStatus_BuyerCancel() {
        when(userService.getCurrentUser()).thenReturn(testBuyer);
        when(orderRepository.findById(testOrder.getOrderId())).thenReturn(Optional.of(testOrder));
        when(orderItemRepository.findByOrder_OrderId(testOrder.getOrderId())).thenReturn(Arrays.asList());
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        orderService.updateOrderStatus(testOrder.getOrderId(), "CANCELLED");

        assertEquals("CANCELLED", testOrder.getStatus());
        verify(orderRepository, times(1)).save(testOrder);
    }
}
