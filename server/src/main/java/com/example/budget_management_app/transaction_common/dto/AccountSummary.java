package com.example.budget_management_app.transaction_common.dto;


import com.example.budget_management_app.common.enums.SupportedCurrency;

public record AccountSummary(
        Long id,
        String name,
        SupportedCurrency currency
) {
}
