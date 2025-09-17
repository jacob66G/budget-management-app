package com.example.budget_management_app.session.dto;

public record UserSessionResponseDto(
        String accessToken,
        Long userId,
        String name,
        String surname,
        String email
) {
}
