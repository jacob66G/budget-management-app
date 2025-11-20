package com.example.budget_management_app.transaction.dto;

import com.example.budget_management_app.transaction_common.domain.TransactionType;
import com.example.budget_management_app.transaction_common.dto.AccountSummary;
import com.example.budget_management_app.transaction_common.dto.CategorySummary;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionSummary(
        Long id,
        BigDecimal amount,
        TransactionType type,
        String description,
        LocalDateTime transactionDate,
        AccountSummary account,
        CategorySummary category,
        Long recurringTransactionId
) {
}
