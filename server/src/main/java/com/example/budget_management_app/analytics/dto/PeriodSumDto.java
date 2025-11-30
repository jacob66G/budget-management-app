package com.example.budget_management_app.analytics.dto;

import java.math.BigDecimal;

public record PeriodSumDto(
        Integer year,
        Integer month,
        BigDecimal amount
) {
}
