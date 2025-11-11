package com.example.budget_management_app.transaction.dto;

import com.example.budget_management_app.transaction.domain.TransactionModeFilter;
import com.example.budget_management_app.transaction.domain.TransactionTypeFilter;

import java.time.LocalDate;
import java.util.List;

public record TransactionSearchCriteria(
        TransactionTypeFilter type,
        TransactionModeFilter mode,
        List<Long> accountIds,
        List<Long> categoryIds,
        LocalDate since,
        LocalDate to
) {
}
