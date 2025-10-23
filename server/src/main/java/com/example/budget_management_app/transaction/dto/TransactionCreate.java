package com.example.budget_management_app.transaction.dto;

import com.example.budget_management_app.transaction.domain.TransactionType;

import java.math.BigDecimal;

public record TransactionCreate(
        BigDecimal amount,
        String title,
        TransactionType type,
        String description,
        long accountId,
        long categoryId
) {
}
