package com.example.budget_management_app.recurring_transaction.dto;

import com.example.budget_management_app.transaction_common.domain.TransactionType;
import com.example.budget_management_app.transaction_common.dto.AccountSummary;
import com.example.budget_management_app.transaction_common.dto.CategorySummary;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpcomingTransactionSummary(
        Long recurringTemplateId,
        BigDecimal amount,
        String title,
        TransactionType type,
        LocalDate nextOccurrence,
        AccountSummary accountSummary,
        CategorySummary categorySummary
) {
}
