package com.example.budget_management_app.chat.dto;

import java.math.BigDecimal;

public record TransactionDataResponse(
        String title,
        BigDecimal amount,
        String categoryName
) {
}
