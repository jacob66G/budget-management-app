package com.example.budget_management_app.common.exception;

public abstract class AuthenticationException extends ApplicationException {
    public AuthenticationException(String message, ErrorCode errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }

    public AuthenticationException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

}
