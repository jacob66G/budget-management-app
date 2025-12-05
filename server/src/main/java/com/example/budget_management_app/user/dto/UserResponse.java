package com.example.budget_management_app.user.dto;

import com.example.budget_management_app.user.domain.UserStatus;

import java.time.Instant;

public record UserResponse(
        Long id,
        String name,
        String surname,
        String email,
        UserStatus status,
        boolean mfaEnabled,
        Instant createdAt
) {
}
