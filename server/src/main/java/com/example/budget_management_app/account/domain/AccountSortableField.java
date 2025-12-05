package com.example.budget_management_app.account.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum AccountSortableField {
    NAME("name"),
    BALANCE("balance"),
    TOTAL_INCOME("totalIncome"),
    TOTAL_EXPENSE("totalExpense"),
    CREATED_AT("createdAt");

    private final String fieldName;
}
