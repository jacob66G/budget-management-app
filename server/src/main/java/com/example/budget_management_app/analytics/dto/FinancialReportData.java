package com.example.budget_management_app.analytics.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record FinancialReportData(
        String accountName,
        LocalDateTime from,
        LocalDateTime to,
        LocalDateTime generatedAt,
        String currency,
        BigDecimal closingBalance,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balanceInPeriod,
        Double savingsRatio,
        BigDecimal averageDailyExpenses,
        List<CategoryBreakdownPointDto> expenseBreakdown,
        List<CategoryBreakdownPointDto> incomeBreakdown
) {}
