package com.example.budget_management_app.analytics.listener;

import com.example.budget_management_app.analytics.events.FinancialReportEvent;
import com.example.budget_management_app.common.mail.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class FinancialReportListener {

    private final EmailService emailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFinancialReport(FinancialReportEvent event) {
        emailService.sendFinancialReport(event.userEmail(), event.userName(), event.pdfData());
    }
}
