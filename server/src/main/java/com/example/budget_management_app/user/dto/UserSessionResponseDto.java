package com.example.budget_management_app.user.dto;

import java.time.Instant;

public record UserSessionResponseDto(
        Long id,
        String ipAddress,
        String deviceInfo,
        String deviceType,
        Instant createdAt,
        Instant lastUsedAt,
        Boolean isCurrent
) {
}
