package com.example.budget_management_app.auth.dto;

public record RegistrationResponseDto(
        Long userId,
        String userName,
        String userSurname,
        String userEmail,
        String userStatus
) {
}
