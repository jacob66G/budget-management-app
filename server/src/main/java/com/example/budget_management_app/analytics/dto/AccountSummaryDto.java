package com.example.budget_management_app.analytics.dto;

import com.example.budget_management_app.account.domain.AccountType;
import com.example.budget_management_app.common.enums.SupportedCurrency;

import java.math.BigDecimal;

public record AccountSummaryDto(
        Long id,
        String name,
        BigDecimal balance,
        SupportedCurrency currency,
        AccountType type,
        Boolean includeInTotalBalance,
        String iconPath
) {}
