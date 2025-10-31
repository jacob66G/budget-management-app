package com.example.budget_management_app.account.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountDetailsResponseDto(
        Long id,
        String type,
        String name,
        BigDecimal balance,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        String currency,
        Boolean isDefault,
        String description,
        String budgetType,
        BigDecimal budget,
        Double alertThreshold,
        String iconPath,
        Boolean includeInTotalBalance,
        Instant createdAt,
        String status
) {
}
