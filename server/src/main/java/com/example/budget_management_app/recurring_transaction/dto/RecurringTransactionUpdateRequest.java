package com.example.budget_management_app.recurring_transaction.dto;

import java.math.BigDecimal;

public record RecurringTransactionUpdateRequest(
        BigDecimal amount,
        String title,
        String description
) {
}
