package com.example.budget_management_app.user.dto;

import com.example.budget_management_app.session.domain.DeviceType;

import java.time.Instant;

public record UserSessionResponse(
        Long id,
        String ipAddress,
        String deviceInfo,
        DeviceType deviceType,
        Instant createdAt,
        Instant lastUsedAt,
        Boolean isCurrent
) {
}
