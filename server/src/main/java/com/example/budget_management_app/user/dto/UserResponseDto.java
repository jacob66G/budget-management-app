package com.example.budget_management_app.user.dto;

import java.time.Instant;

public record UserResponseDto(
        Long id,
        String name,
        String surname,
        String email,
        String status,
        boolean mfaEnabled,
        Instant createdAt
) {
}
