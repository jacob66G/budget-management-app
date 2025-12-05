package com.example.budget_management_app.account.dto;

import com.example.budget_management_app.account.domain.AccountSortableField;
import com.example.budget_management_app.account.domain.AccountStatus;
import com.example.budget_management_app.account.domain.AccountType;
import com.example.budget_management_app.account.domain.BudgetType;
import com.example.budget_management_app.common.enums.SupportedCurrency;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record SearchCriteria(
        AccountType type,
        String name,
        List<AccountStatus> status,
        List<SupportedCurrency> currencies,
        List<BudgetType> budgetTypes,
        BigDecimal minBalance,
        BigDecimal maxBalance,
        BigDecimal minTotalIncome,
        BigDecimal maxTotalIncome,
        BigDecimal minTotalExpense,
        BigDecimal maxTotalExpense,
        BigDecimal minBudget,
        BigDecimal maxBudget,
        Boolean includedInTotalBalance,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate createdAfter,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate createdBefore,
        AccountSortableField sortBy,
        String sortDirection
) {
}
