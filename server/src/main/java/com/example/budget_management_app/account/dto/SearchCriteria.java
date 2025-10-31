package com.example.budget_management_app.account.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record SearchCriteria(
        String type,
        String name,
        List<String> status,
        List<String> currencies,
        List<String> budgetTypes,
        BigDecimal minBalance,
        BigDecimal maxBalance,
        BigDecimal minTotalIncome,
        BigDecimal maxTotalIncome,
        BigDecimal minTotalExpense,
        BigDecimal maxTotalExpense,
        BigDecimal minBudget,
        BigDecimal maxBudget,
        Boolean includedInTotalBalance,
        Instant createdAfter,
        Instant createdBefore,
        String sortBy,
        String sortDirection
) {
}
