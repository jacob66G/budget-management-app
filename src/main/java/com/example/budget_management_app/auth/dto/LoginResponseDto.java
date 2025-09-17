package com.example.budget_management_app.auth.dto;

public record LoginResponseDto(
        String accessToken,
        Long userId,
        String name,
        String surname,
        String email
) {
}
