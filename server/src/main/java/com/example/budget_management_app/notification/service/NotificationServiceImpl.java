package com.example.budget_management_app.notification.service;

import com.example.budget_management_app.common.exception.ErrorCode;
import com.example.budget_management_app.common.exception.NotFoundException;
import com.example.budget_management_app.notification.dao.NotificationDao;
import com.example.budget_management_app.notification.domain.Notification;
import com.example.budget_management_app.notification.domain.NotificationType;
import com.example.budget_management_app.notification.dto.NotificationDto;
import com.example.budget_management_app.notification.mapper.NotificationMapper;
import com.example.budget_management_app.user.dao.UserDao;
import com.example.budget_management_app.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationDao notificationDao;
    private final UserDao userDao;
    private final NotificationMapper mapper;

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public void createAndSend(Long userId, String title, String message, NotificationType type, String targetUrl) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with not found", ErrorCode.NOT_FOUND));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setTargetUrl(targetUrl);

        Notification persistedNotification = notificationDao.save(notification);

        NotificationDto dto = mapper.toDto(persistedNotification);

        messagingTemplate.convertAndSendToUser(user.getEmail(), "/queue/notifications", dto);
        log.info("User action: {} invoke notification", title);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getUserNotifications(Long userId, boolean isRead) {
        return notificationDao.findByUser(userId, false).stream().map(mapper::toDto).toList();
    }

    @Override
    public long countUnread(Long userId) {
        return notificationDao.countUnreadForUser(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationDao.findById(notificationId)
                .orElseThrow(() ->  new NotFoundException("Notification not found", ErrorCode.NOT_FOUND));

        if(!notification.getUser().getId().equals(userId)) {
            throw new NotFoundException("Notification not found for user", ErrorCode.NOT_FOUND);
        }

        if(!notification.isRead()) {
            notification.setRead(true);
            notificationDao.update(notification);
        }
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationDao.markAllAsReadForUser(userId);
    }
}
