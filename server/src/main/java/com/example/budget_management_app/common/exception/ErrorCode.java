package com.example.budget_management_app.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INTERNAL_EXCEPTION("INTERNAL_EXCEPTION", HttpStatus.INTERNAL_SERVER_ERROR),
    NOT_FOUND("NOT_FOUND", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND("USER_NOT_FOUND", HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_USED("EMAIL_ALREADY_USED", HttpStatus.CONFLICT),
    INVALID_TOKEN("INVALID_TOKEN", HttpStatus.UNAUTHORIZED),
    SESSION_EXPIRED("SESSION_EXPIRED", HttpStatus.UNAUTHORIZED),
    TOKEN_NOT_FOUND("TOKEN_NOT_FOUND", HttpStatus.UNAUTHORIZED),
    CODE_EXPIRED("CODE_EXPIRED", HttpStatus.UNAUTHORIZED),
    INVALID_CODE("INVALID_CODE", HttpStatus.UNAUTHORIZED),
    CUSTOM_HEADER_NOT_FOUND("CUSTOM_HEADER_NOT_FOUND", HttpStatus.FORBIDDEN),
    USER_NOT_ACTIVE("USER_NOT_ACTIVE", HttpStatus.FORBIDDEN),
    EMAIL_SEND_FAIL("EMAIL_SEND_FAIL", HttpStatus.INTERNAL_SERVER_ERROR),
    MFA_CONFIGURATION("MFA_CONFIGURATION", HttpStatus.FORBIDDEN);

    private final String errorCode;
    private final HttpStatus status;
}
