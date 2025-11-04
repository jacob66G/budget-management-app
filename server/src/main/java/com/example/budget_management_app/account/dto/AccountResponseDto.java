package com.example.budget_management_app.account.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountResponseDto(
        Long id,
        String type,
        String name,
        BigDecimal balance,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        String currency,
        Boolean isDefault,
        String iconKey,
        Boolean includeInTotalBalance,
        Instant createdAt,
        String status
) {
}
