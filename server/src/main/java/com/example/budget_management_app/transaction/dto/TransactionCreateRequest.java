package com.example.budget_management_app.transaction.dto;

import com.example.budget_management_app.transaction_common.domain.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TransactionCreateRequest(

        @NotNull(message = "Transaction amount is required")
        @Positive(message = "Transaction amount must be a positive value")
        BigDecimal amount,

        @NotBlank(message = "Transaction title is required and must not be empty")
        @Size(min = 3, max = 30, message = "Title size must consist of at least 3 characters and must not exceed 30 characters")
        String title,

        @NotNull(message = "Transaction type is required")
        TransactionType type,

        @Size(max = 100, message = "Description must not exceed 100 characters")
        @NotNull(message = "Description is required")
        String description,

        @NotNull(message = "Account id value is required")
        @Positive(message = "Account id must be a positive value")
        Long accountId,

        @NotNull(message = "Category id value is required")
        @Positive(message = "Category id must be a positive value")
        Long categoryId
) {
}
