package com.example.budget_management_app.transaction_common.dto;

import com.example.budget_management_app.account.domain.SupportedCurrency;

public record AccountSummary(
        long id,
        String name,
        SupportedCurrency currency
) {
}
