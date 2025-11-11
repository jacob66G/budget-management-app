package com.example.budget_management_app.session.dto;

import com.example.budget_management_app.auth.dto.LoginResponseDto;

public record RefreshTokenResult(
        LoginResponseDto loginResponse,
        String cookie
) {
}
