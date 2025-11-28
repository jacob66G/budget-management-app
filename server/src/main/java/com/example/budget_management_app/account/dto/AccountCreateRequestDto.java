package com.example.budget_management_app.account.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record AccountCreateRequestDto(
        @NotBlank(message = "Type cannot be empty")
        String type,
        @NotBlank(message = "Name cannot be empty")
        @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
        String name,
        @NotBlank(message = "Currency cannot be empty")
        @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid")
        String currency,
        @Size(max = 255, message = "Description can be up to 255 characters")
        String description,
        @DecimalMin(value = "0.0", message = "Initial balance must be positive or zero")
        BigDecimal initialBalance,
        @NotBlank(message = "BudgetType cannot be empty")
        String budgetType,
        @DecimalMin(value = "0.0", message = "Budget must be positive or zero")
        BigDecimal budget,
        @DecimalMin(value = "0.0", message = "Alert threshold must be at least 0%")
        @DecimalMax(value = "100.0", message = "Alert threshold cannot exceed 100%")
        Double alertThreshold,
        @NotNull(message = "Inclusion in the total balance is not defined")
        Boolean includeInTotalBalance,
        String iconPath
) {
}
