package com.example.budget_management_app.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TransactionUpdateRequest(

        @NotBlank(message = "Transaction title is required and must not be empty")
        @Size(min = 3, max = 30, message = "Title size must consist of at least 3 characters and must not exceed 30 characters")
        String title,

        @NotNull(message = "Transaction amount is required")
        @Positive(message = "Transaction amount must be a positive value")
        BigDecimal amount,

        @Size(max = 100, message = "Description must not exceed 100 characters")
        @NotNull(message = "Transaction type is required")
        String description
) {
}
