package com.example.budget_management_app.analytics.dto;

import java.math.BigDecimal;
import java.util.List;

public record ChartSeriesDto(
        String label,
        List<BigDecimal> data
) {
}
