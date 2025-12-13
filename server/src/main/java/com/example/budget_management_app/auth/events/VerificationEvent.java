package com.example.budget_management_app.auth.events;

public record VerificationEvent(
        String userEmail,
        String userName,
        String verificationCode,
        boolean resend
) {
}
