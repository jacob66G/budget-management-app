package com.example.budget_management_app.auth.listener;

import com.example.budget_management_app.auth.events.PasswordResetEvent;
import com.example.budget_management_app.auth.events.VerificationEvent;
import com.example.budget_management_app.common.mail.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class AuthEventListener {

    private final EmailService emailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserRegistered(VerificationEvent event) {
        emailService.sendVerificationEmail(event.userEmail(), event.userName(), event.verificationCode(), event.resend());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleResetPassword(PasswordResetEvent event) {
        emailService.sendResetPasswordEmail(event.userEmail(), event.userName(), event.token());
    }

}
