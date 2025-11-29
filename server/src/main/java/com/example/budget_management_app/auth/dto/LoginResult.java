package com.example.budget_management_app.auth.dto;

import org.springframework.http.ResponseCookie;

public record LoginResult(
        LoginResponse response,
        ResponseCookie cookie
) {
    public static LoginResult mfaRequired(LoginResponse response) {
        return new LoginResult(response, null);
    }
}