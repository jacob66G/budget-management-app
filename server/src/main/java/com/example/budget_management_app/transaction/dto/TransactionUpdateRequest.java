package com.example.budget_management_app.transaction.dto;

import java.math.BigDecimal;

public record TransactionUpdateRequest(
        String title,
        BigDecimal amount,
        String description
) {
}
