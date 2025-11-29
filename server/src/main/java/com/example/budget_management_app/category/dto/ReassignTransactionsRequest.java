package com.example.budget_management_app.category.dto;

import jakarta.validation.constraints.NotNull;

public record ReassignTransactionsRequest(
        @NotNull(message = "Category must be selected")
        Long newCategoryId
) {
}
