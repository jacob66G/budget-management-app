package com.example.budget_management_app.account.dto;

import com.example.budget_management_app.account.domain.BudgetType;
import com.example.budget_management_app.common.enums.SupportedCurrency;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record AccountUpdateRequest(
        @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
        String name,
        SupportedCurrency currency,
        @Size(max = 255, message = "Description can be up to 255 characters")
        String description,
        @DecimalMin(value = "0.0", message = "Initial balance must be positive or zero")
        BigDecimal initialBalance,
        BudgetType budgetType,
        @DecimalMin(value = "0.0", message = "Budget must be positive or zero")
        BigDecimal budget,
        @DecimalMin(value = "0.0", message = "Alert threshold must be at least 0%")
        @DecimalMax(value = "100.0", message = "Alert threshold cannot exceed 100%")
        Double alertThreshold,
        Boolean includeInTotalBalance,
        String iconPath
) {
}
