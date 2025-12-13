package com.example.budget_management_app.notification.service;

import com.example.budget_management_app.notification.domain.NotificationType;
import com.example.budget_management_app.notification.dto.NotificationDto;

import java.util.List;

public interface NotificationService {
    void createAndSend(Long userId, String title, String message, NotificationType type, String targetUrl);

    List<NotificationDto> getUserNotifications(Long userId, boolean isRead);

    long countUnread(Long userId);

    void markAsRead(Long notificationId, Long userId);

    void markAllAsRead(Long userId);
}
