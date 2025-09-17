package com.example.budget_management_app.common.exception;

public class EmailException extends ApplicationException {
    public EmailException(String message,  ErrorCode errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
}
