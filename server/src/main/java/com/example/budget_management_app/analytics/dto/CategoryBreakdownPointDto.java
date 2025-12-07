package com.example.budget_management_app.analytics.dto;

import java.math.BigDecimal;

public record CategoryBreakdownPointDto(
        String categoryName,
        BigDecimal amount
) {}
