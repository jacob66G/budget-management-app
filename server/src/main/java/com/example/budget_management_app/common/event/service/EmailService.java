package com.example.budget_management_app.common.event.service;

public interface EmailService {

    void sendVerificationEmail(String userEmail, String userName, String verificationCode, boolean resend);

    void sendResetPasswordEmail(String userEmail, String userName, String token);
}
