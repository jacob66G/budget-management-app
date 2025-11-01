package com.example.budget_management_app.transaction.dto;

public record TransactionCategoryUpdateRequest(
        long currentTransactionCategoryId,
        long newTransactionCategoryId,
        long accountId
) {
}
