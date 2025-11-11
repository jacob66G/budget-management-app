package com.example.budget_management_app.transaction.dto;

import com.example.budget_management_app.transaction.domain.SortDirection;
import com.example.budget_management_app.transaction.domain.SortedBy;

public record TransactionPageRequest(
        int page,
        int limit,
        SortedBy sortedBy,
        SortDirection sortDirection
) {
}
