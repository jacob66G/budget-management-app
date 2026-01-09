package com.example.budget_management_app.notification.controller;

import com.example.budget_management_app.notification.dto.NotificationDto;
import com.example.budget_management_app.notification.service.NotificationService;
import com.example.budget_management_app.security.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDto>> getUnreadNotifications(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userDetails.getId(), false));
    }

    @PostMapping("/{notificationId}/mark-as-read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        notificationService.markAsRead(notificationId, userDetails.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/mark-as-read")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal CustomUserDetails userDetails) {
        notificationService.markAllAsRead(userDetails.getId());
        return ResponseEntity.ok().build();
    }
}
