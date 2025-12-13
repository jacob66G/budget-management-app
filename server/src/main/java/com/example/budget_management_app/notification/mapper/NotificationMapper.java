package com.example.budget_management_app.notification.mapper;

import com.example.budget_management_app.notification.domain.Notification;
import com.example.budget_management_app.notification.dto.NotificationDto;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationDto toDto(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getTargetUrl()
        );
    }
}
