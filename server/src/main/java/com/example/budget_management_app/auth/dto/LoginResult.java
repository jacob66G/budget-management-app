package com.example.budget_management_app.auth.dto;

import org.springframework.http.ResponseCookie;

public record LoginResult(
        LoginResponseDto response,
        ResponseCookie cookie
) {
    public static LoginResult mfaRequired(LoginResponseDto response) {
        return new LoginResult(response, null);
    }
}