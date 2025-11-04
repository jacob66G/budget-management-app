package com.example.budget_management_app.account.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record AccountUpdateRequestDto(
        @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
        String name,
        @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid")
        String currency,
        @Size(max = 255, message = "Description can be up to 255 characters")
        String description,
        @DecimalMin(value = "0.0", message = "Initial balance must be positive or zero")
        BigDecimal initialBalance,
        String budgetType,
        @DecimalMin(value = "0.0", message = "Budget must be positive or zero")
        BigDecimal budget,
        @DecimalMin(value = "0.0", message = "Alert threshold must be at least 0%")
        @DecimalMax(value = "100.0", message = "Alert threshold cannot exceed 100%")
        Double alertThreshold,
        Boolean includeInTotalBalance,
        String iconKey
) {
}
