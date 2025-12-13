package com.example.budget_management_app.transaction.events;

import com.example.budget_management_app.notification.domain.NotificationType;

import java.math.BigDecimal;

public record BudgetExceededEvent(
        Long userId,
        NotificationType type,
        Long accountId,
        String accountName,
        BigDecimal transactionAmount,
        BigDecimal currentUsage,
        BigDecimal budgetLimit
) {
}
