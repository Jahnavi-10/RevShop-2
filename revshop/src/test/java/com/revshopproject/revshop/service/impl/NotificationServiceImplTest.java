package com.revshopproject.revshop.service.impl;

import com.revshopproject.revshop.entity.Notification;
import com.revshopproject.revshop.entity.User;
import com.revshopproject.revshop.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User testUser;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);

        testNotification = new Notification();
        testNotification.setNotificationId(100L);
        testNotification.setUser(testUser);
        testNotification.setMessage("Test message");
        testNotification.setIsRead(0);
        testNotification.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testSendNotification() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        notificationService.sendNotification(testUser, "Test message");

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testGetNotificationsForUser() {
        when(notificationRepository.findByUser_UserIdOrderByCreatedAtDesc(testUser.getUserId()))
                .thenReturn(Arrays.asList(testNotification));

        List<Notification> notifications = notificationService.getNotificationsForUser(testUser.getUserId());

        assertNotNull(notifications);
        assertEquals(1, notifications.size());
        assertEquals("Test message", notifications.get(0).getMessage());
        verify(notificationRepository, times(1)).findByUser_UserIdOrderByCreatedAtDesc(testUser.getUserId());
    }

    @Test
    void testGetUnreadNotificationsForUser() {
        when(notificationRepository.findByUser_UserIdAndIsReadOrderByCreatedAtDesc(testUser.getUserId(), 0))
                .thenReturn(Arrays.asList(testNotification));

        List<Notification> notifications = notificationService.getUnreadNotificationsForUser(testUser.getUserId());

        assertNotNull(notifications);
        assertEquals(1, notifications.size());
        assertEquals(0, notifications.get(0).getIsRead());
        verify(notificationRepository, times(1)).findByUser_UserIdAndIsReadOrderByCreatedAtDesc(testUser.getUserId(), 0);
    }

    @Test
    void testMarkAsRead() {
        when(notificationRepository.findById(testNotification.getNotificationId())).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        notificationService.markAsRead(testNotification.getNotificationId());

        assertEquals(1, testNotification.getIsRead());
        verify(notificationRepository, times(1)).save(testNotification);
    }

    @Test
    void testMarkAllAsRead() {
        when(notificationRepository.findByUser_UserIdAndIsReadOrderByCreatedAtDesc(testUser.getUserId(), 0))
                .thenReturn(Arrays.asList(testNotification));

        notificationService.markAllAsRead(testUser.getUserId());

        assertEquals(1, testNotification.getIsRead());
        verify(notificationRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testGetUnreadCount() {
        when(notificationRepository.countByUser_UserIdAndIsRead(testUser.getUserId(), 0)).thenReturn(5L);

        long count = notificationService.getUnreadCount(testUser.getUserId());

        assertEquals(5L, count);
        verify(notificationRepository, times(1)).countByUser_UserIdAndIsRead(testUser.getUserId(), 0);
    }
}
