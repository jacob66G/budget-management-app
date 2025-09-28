package com.example.budget_management_app.common.exception;

public class VerificationCodeException extends AuthenticationException {
    public VerificationCodeException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
