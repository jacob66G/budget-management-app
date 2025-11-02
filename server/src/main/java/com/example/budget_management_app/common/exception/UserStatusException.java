package com.example.budget_management_app.common.exception;

public class UserStatusException extends ApplicationException {
    public UserStatusException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}