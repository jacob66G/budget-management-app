package com.example.budget_management_app.recurring_transaction.dto;

import com.example.budget_management_app.transaction.dto.TransactionSummary;

import java.time.LocalDate;

public record RecurringTransactionCreateResponse(
        long id,
        LocalDate nextOccurrence,
        boolean isActive,
        boolean newTransaction,
        TransactionSummary newTransactionView
) {
}
