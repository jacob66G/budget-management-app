package com.example.budget_management_app.common.event.model;

public record VerificationEvent(
        String userEmail,
        String userName,
        String verificationCode,
        boolean resend
) {
}
