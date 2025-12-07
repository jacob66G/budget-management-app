package com.example.budget_management_app.recurring_transaction.dto;

import com.example.budget_management_app.recurring_transaction.domain.RecurringInterval;
import com.example.budget_management_app.transaction_common.domain.TransactionType;
import com.example.budget_management_app.transaction_common.dto.AccountSummary;
import com.example.budget_management_app.transaction_common.dto.CategorySummary;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecurringTransactionSummary(
        long id,
        String title,
        BigDecimal amount,
        TransactionType type,
        boolean isActive,
        String description,
        LocalDate nextOccurrence,
        RecurringInterval recurringInterval,
        int recurringValue,
        AccountSummary accountSummary,
        CategorySummary categorySummary
) {
}
