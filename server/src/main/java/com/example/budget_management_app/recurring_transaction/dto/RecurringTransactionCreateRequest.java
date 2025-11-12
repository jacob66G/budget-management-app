package com.example.budget_management_app.recurring_transaction.dto;

import com.example.budget_management_app.recurring_transaction.domain.RecurringInterval;
import com.example.budget_management_app.transaction_common.domain.TransactionType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecurringTransactionCreateRequest(

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

        @Size(max = 100, message = "Description must not exceed 100 characters")
        @NotNull(message = "Description is required")
        String description,

        @NotNull(message = "Transaction start date is required")
        @FutureOrPresent(message = "Transaction start date must not be in the past")
        LocalDate startDate,

        @Future(message = "Transaction end date must be in the future")
        LocalDate endDate,

        @NotNull(message = "Transaction recurring interval is required")
        RecurringInterval recurringInterval,

        @NotNull(message = "Transaction recurring value is required")
        @Positive(message = "Transaction recurring value must be positive value")
        @Max(value = 7, message = "Transaction recurring value must not exceed 7")
        Integer recurringValue,

        @NotNull(message = "Account id value is required")
        @Positive(message = "Account id must be a positive value")
        Long accountId,

        @NotNull(message = "Category id value is required")
        @Positive(message = "Category id must be a positive value")
        Long categoryId
) {
}
