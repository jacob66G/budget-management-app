package com.example.budget_management_app.recurring_transaction.scheduler;

import com.example.budget_management_app.recurring_transaction.service.RecurringTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicationScheduler {

    private final RecurringTransactionService recurringTransactionService;

    @Scheduled(cron = "0 0 12 * * ?")
    public void generateRecurringTransactions() {
        this.recurringTransactionService.generateRecurringTransactions();
    }
}
