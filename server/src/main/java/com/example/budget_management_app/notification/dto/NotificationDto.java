package com.example.budget_management_app.notification.dto;

import com.example.budget_management_app.notification.domain.NotificationType;

import java.time.Instant;

public record NotificationDto(
        Long id,
        String title,
        String message,
        NotificationType type,
        boolean isRead,
        Instant createdAt,
        String targetUrl
) {
}
