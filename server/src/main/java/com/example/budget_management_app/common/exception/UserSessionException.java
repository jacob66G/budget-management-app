package com.example.budget_management_app.common.exception;

public class UserSessionException extends AuthenticationException {
    public UserSessionException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
