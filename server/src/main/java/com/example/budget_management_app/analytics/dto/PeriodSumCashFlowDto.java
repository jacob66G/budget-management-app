package com.example.budget_management_app.analytics.dto;

import java.math.BigDecimal;

public record PeriodSumCashFlowDto(
        Integer year,
        Integer month,
        BigDecimal totalIncome,
        BigDecimal totalExpense
) {
}
