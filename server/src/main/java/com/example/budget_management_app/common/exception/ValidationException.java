package com.example.budget_management_app.common.exception;

public class ValidationException extends ApplicationException {
    public ValidationException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
