package com.example.budget_management_app.session.dto;

public record RefreshTokenResult(
        String accessToken,
        String cookie
) {
}
