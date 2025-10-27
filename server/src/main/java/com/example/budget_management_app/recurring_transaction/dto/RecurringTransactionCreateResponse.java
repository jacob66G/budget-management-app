package com.example.budget_management_app.recurring_transaction.dto;

import com.example.budget_management_app.transaction.dto.TransactionView;

import java.time.LocalDate;

public record RecurringTransactionCreateResponse(
        long id,
        LocalDate nextOccurrence,
        boolean isActive,
        boolean newTransaction,
        TransactionView newTransactionView
) {
}
