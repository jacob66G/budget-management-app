package com.example.budget_management_app.recurring_transaction.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record RecurringTransactionDetailsResponse(
        LocalDate nextOccurrence,
        LocalDate startDate,
        LocalDate endDate,
        LocalDateTime createdAt
) {
}
