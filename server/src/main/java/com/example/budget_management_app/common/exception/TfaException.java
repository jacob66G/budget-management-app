package com.example.budget_management_app.common.exception;

public class TfaException extends ApplicationException {
    public TfaException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
