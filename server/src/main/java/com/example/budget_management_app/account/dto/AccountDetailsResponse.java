package com.example.budget_management_app.account.dto;

import com.example.budget_management_app.account.domain.AccountStatus;
import com.example.budget_management_app.account.domain.AccountType;
import com.example.budget_management_app.account.domain.BudgetType;
import com.example.budget_management_app.common.enums.SupportedCurrency;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountDetailsResponse(
        Long id,
        AccountType type,
        String name,
        BigDecimal balance,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        SupportedCurrency currency,
        Boolean isDefault,
        String description,
        BudgetType budgetType,
        BigDecimal budget,
        Double alertThreshold,
        String iconPath,
        Boolean includeInTotalBalance,
        Instant createdAt,
        AccountStatus status,
        boolean hasTransactions
) {
}
