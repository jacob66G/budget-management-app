package com.example.budget_management_app.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INTERNAL_EXCEPTION("INTERNAL_EXCEPTION", HttpStatus.INTERNAL_SERVER_ERROR),
    EMAIL_SEND_FAIL("EMAIL_SEND_FAIL", HttpStatus.INTERNAL_SERVER_ERROR),

    NOT_FOUND("NOT_FOUND", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND("USER_NOT_FOUND", HttpStatus.NOT_FOUND),

    INVALID_TOKEN("INVALID_TOKEN", HttpStatus.UNAUTHORIZED),
    TOKEN_NOT_FOUND("TOKEN_NOT_FOUND", HttpStatus.UNAUTHORIZED),
    CODE_EXPIRED("CODE_EXPIRED", HttpStatus.UNAUTHORIZED),
    INVALID_CODE("INVALID_CODE", HttpStatus.UNAUTHORIZED),

    USER_NOT_ACTIVE("USER_NOT_ACTIVE", HttpStatus.FORBIDDEN),
    MFA_CONFIGURATION("MFA_CONFIGURATION", HttpStatus.FORBIDDEN),

    EMAIL_ALREADY_USED("EMAIL_ALREADY_USED", HttpStatus.CONFLICT),
    NAME_ALREADY_USED("NAME_ALREADY_USED", HttpStatus.CONFLICT),
    WRONG_CATEGORY_TYPE("WRONG_CATEGORY_TYPE", HttpStatus.CONFLICT),
    MODIFY_DEFAULT_CATEGORY("MODIFY_DEFAULT_CATEGORY", HttpStatus.CONFLICT);

    private final String errorCode;
    private final HttpStatus status;
}
