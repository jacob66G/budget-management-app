package com.example.budget_management_app.recurring_transaction.dto;

import com.example.budget_management_app.recurring_transaction.domain.RecurringInterval;
import com.example.budget_management_app.transaction.domain.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecurringTransactionCreateRequest(
        BigDecimal amount,
        String title,
        TransactionType type,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        RecurringInterval recurringInterval,
        int recurringValue,
        long accountId,
        long categoryId
) {
}
