package com.example.budget_management_app.account.domain;

import com.example.budget_management_app.common.exception.ErrorCode;
import com.example.budget_management_app.common.exception.ValidationException;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum AccountType {
    PERSONAL;

    @JsonCreator
    public static AccountType fromString(String value) {
        if (value == null) {
            return null;
        }
        try {
            return AccountType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Incorrect type: " + value, ErrorCode.WRONG_TYPE);
        }
    }
}
