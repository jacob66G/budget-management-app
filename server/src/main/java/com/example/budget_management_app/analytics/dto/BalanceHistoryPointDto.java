package com.example.budget_management_app.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BalanceHistoryPointDto(
        LocalDate date,
        BigDecimal amount
) {
}
