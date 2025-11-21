package com.example.budget_management_app.common.exception;

public abstract class AuthenticationException extends ApplicationException {

    public AuthenticationException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

}
