package com.revshopproject.revshop.service.impl;

import com.revshopproject.revshop.entity.Order;
import com.revshopproject.revshop.entity.OrderItem;
import com.revshopproject.revshop.entity.Product;
import com.revshopproject.revshop.entity.User;
import com.revshopproject.revshop.repository.OrderItemRepository;
import com.revshopproject.revshop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SellerServiceImplTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private SellerServiceImpl sellerService;

    private User testSeller;
    private Product testProduct;
    private OrderItem testOrderItem;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testSeller = new User();
        testSeller.setUserId(2L);
        testSeller.setRole("SELLER");

        testProduct = new Product();
        testProduct.setProductId(100L);
        testProduct.setSeller(testSeller);

        testOrder = new Order();
        testOrder.setOrderId(500L);

        testOrderItem = new OrderItem();
        testOrderItem.setOrderItemId(1000L);
        testOrderItem.setOrder(testOrder);
        testOrderItem.setProduct(testProduct);
        testOrderItem.setPrice(new BigDecimal("50.00"));
        testOrderItem.setQuantity(2); // Total = 100.00
    }

    @Test
    void testGetSellerStats() {
        when(orderItemRepository.findByProduct_Seller_UserIdOrderByOrder_OrderIdDesc(testSeller.getUserId()))
                .thenReturn(Arrays.asList(testOrderItem));
        when(productRepository.findBySeller_UserId(testSeller.getUserId())).thenReturn(Arrays.asList(testProduct));

        Map<String, Object> stats = sellerService.getSellerStats(testSeller.getUserId());

        assertNotNull(stats);
        assertEquals(new BigDecimal("100.00"), stats.get("totalRevenue"));
        assertEquals(1, stats.get("totalItemsSold"));
        assertEquals(1, stats.get("activeProducts"));
    }

    @Test
    void testGetSellerOrders() {
        when(orderItemRepository.findByProduct_Seller_UserIdOrderByOrder_OrderIdDesc(testSeller.getUserId()))
                .thenReturn(Arrays.asList(testOrderItem));

        List<OrderItem> orders = sellerService.getSellerOrders(testSeller.getUserId());

        assertNotNull(orders);
        assertEquals(1, orders.size());
        verify(orderItemRepository, times(1)).findByProduct_Seller_UserIdOrderByOrder_OrderIdDesc(testSeller.getUserId());
    }
}
