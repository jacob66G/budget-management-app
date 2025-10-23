package com.example.budget_management_app.transaction.dto;

import java.time.LocalDateTime;

public record TransactionResponse(
        long id,
        LocalDateTime transactionDate
) {
}
