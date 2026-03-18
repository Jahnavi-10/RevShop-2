package com.revshopproject.revshop.service;

import java.util.List;

import com.revshopproject.revshop.dto.OrderRequestDTO;
import com.revshopproject.revshop.dto.OrderResponseDTO;

public interface OrderService {
    OrderResponseDTO placeOrder(OrderRequestDTO dto);
    List<OrderResponseDTO> getOrdersByUserId();
    OrderResponseDTO getOrderById(Long orderId);
    OrderResponseDTO updateOrderStatus(Long orderId, String status);
    OrderResponseDTO acceptOrder(Long orderId);
    OrderResponseDTO markAsDelivered(Long orderId);
}