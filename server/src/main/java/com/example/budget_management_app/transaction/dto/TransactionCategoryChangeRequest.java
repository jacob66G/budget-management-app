package com.example.budget_management_app.transaction.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransactionCategoryChangeRequest(

        @NotNull(message = "Current category id is required")
        @Positive(message = "Current category id must be positive value")
        Long currentCategoryId,

        @NotNull(message = "New category id required")
        @Positive(message = "New category id must be positive value")
        Long newCategoryId,

        @NotNull(message = "Account id is required")
        @Positive(message = "Account id must be positive value")
        Long accountId
) {
}
