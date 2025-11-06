package com.example.budget_management_app.transaction.dto;

import com.example.budget_management_app.transaction_common.domain.TransactionType;
import com.example.budget_management_app.transaction_common.dto.AccountSummary;
import com.example.budget_management_app.transaction_common.dto.CategorySummary;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionSummary(
        long id,
        BigDecimal amount,
        TransactionType type,
        String description,
        LocalDateTime transactionDate,
        AccountSummary account,
        CategorySummary category
) {
}
