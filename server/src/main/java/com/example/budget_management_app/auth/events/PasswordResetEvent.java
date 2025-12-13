package com.example.budget_management_app.auth.events;

public record PasswordResetEvent(
        String userEmail,
        String userName,
        String token
) {
}
