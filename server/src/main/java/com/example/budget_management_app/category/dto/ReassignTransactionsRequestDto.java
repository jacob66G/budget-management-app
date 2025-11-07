package com.example.budget_management_app.category.dto;

import jakarta.validation.constraints.NotNull;

public record ReassignTransactionsRequestDto(
        @NotNull(message = "Category must be selected")
        Long newCategoryId
) {
}
