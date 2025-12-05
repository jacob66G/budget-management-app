package com.example.budget_management_app.account.domain;

import com.example.budget_management_app.common.exception.ErrorCode;
import com.example.budget_management_app.common.exception.ValidationException;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum BudgetType {
    WEEKLY, MONTHLY, NONE;

    @JsonCreator
    public static BudgetType fromString(String value) {
        if (value == null) {
            return null;
        }
        try {
            return BudgetType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Incorrect budget type: " + value, ErrorCode.WRONG_TYPE);
        }
    }
}
