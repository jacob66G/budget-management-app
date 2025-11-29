package com.example.budget_management_app.session.dto;

import com.example.budget_management_app.auth.dto.LoginResponse;

public record RefreshTokenResult(
        LoginResponse loginResponse,
        String cookie
) {
}
