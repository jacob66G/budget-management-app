package com.example.budget_management_app.analytics.dto;

import java.math.BigDecimal;

public record CategoryChartPoint(
        String categoryName,
        BigDecimal amount
) {}
