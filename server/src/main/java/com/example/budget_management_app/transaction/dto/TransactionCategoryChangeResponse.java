package com.example.budget_management_app.transaction.dto;

public record TransactionCategoryChangeResponse(
        long categoryId,
        String categoryName,
        String categoryIconPath
) {
}
