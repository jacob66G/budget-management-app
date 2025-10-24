package com.example.budget_management_app.transaction.dto;

public record TransactionCategoryUpdateResponse(
        long categoryId,
        String categoryName,
        String categoryIconPath
) {
}
