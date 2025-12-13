package com.example.budget_management_app.analytics.dto;

import com.example.budget_management_app.common.enums.SupportedCurrency;
import com.example.budget_management_app.transaction_common.domain.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionSummaryDto(
        String title,
        BigDecimal amount,
        TransactionType type,
        LocalDateTime date,
        String categoryName,
        String categoryIconPath,
        String accountName,
        SupportedCurrency currency
) {
}
