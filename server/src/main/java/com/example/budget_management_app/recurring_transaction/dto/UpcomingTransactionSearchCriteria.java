package com.example.budget_management_app.recurring_transaction.dto;

import com.example.budget_management_app.recurring_transaction.domain.UpcomingTransactionsTimeRange;

import java.util.List;

public record UpcomingTransactionSearchCriteria(
        UpcomingTransactionsTimeRange range,
        List<Long> accountIds
) {
}
