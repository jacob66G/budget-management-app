package com.example.budget_management_app.transaction.dto;

import com.example.budget_management_app.transaction.domain.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionView(
        long id,
        BigDecimal amount,
        TransactionType type,
        String description,
        LocalDateTime transactionDate,
        AccountSummary account,
        CategorySummary category
) {
}
