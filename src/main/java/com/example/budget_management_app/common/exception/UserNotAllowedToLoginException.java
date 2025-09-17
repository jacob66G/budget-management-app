package com.example.budget_management_app.common.exception;

public class UserNotAllowedToLoginException extends AuthenticationException {
    public UserNotAllowedToLoginException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
