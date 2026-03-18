package com.revshopproject.revshop.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.revshopproject.revshop.entity.Notification;
import com.revshopproject.revshop.entity.User;
import com.revshopproject.revshop.repository.NotificationRepository;
import com.revshopproject.revshop.service.NotificationService;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public void sendNotification(User user, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<Notification> getUnreadNotificationsForUser(Long userId) {
        return notificationRepository.findByUser_UserIdAndIsReadOrderByCreatedAtDesc(userId, 0);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setIsRead(1);
            notificationRepository.save(n);
        });
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUser_UserIdAndIsReadOrderByCreatedAtDesc(userId, 0);
        unread.forEach(n -> n.setIsRead(1));
        notificationRepository.saveAll(unread);
    }
    
    @Override
    public long getUnreadCount(Long userId) {
        // This calls the countByUser_UserIdAndIsRead method in your repository
        return notificationRepository.countByUser_UserIdAndIsRead(userId, 0);
    }
}