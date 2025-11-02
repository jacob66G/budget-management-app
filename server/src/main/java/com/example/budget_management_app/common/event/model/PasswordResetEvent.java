package com.example.budget_management_app.common.event.model;

public record PasswordResetEvent(
        String userEmail,
        String userName,
        String token
) {
}
