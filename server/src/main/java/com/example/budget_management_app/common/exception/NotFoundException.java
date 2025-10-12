package com.example.budget_management_app.common.exception;

public class NotFoundException extends ApplicationException {
    public NotFoundException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

    public NotFoundException(String resource, Long id, ErrorCode errorCode) {
        super(resource + " not found with id: " + id, errorCode);
    }

    public NotFoundException(String resource, String identifierName, Object identifierValue, ErrorCode errorCode) {
        super(resource + " not found with " + identifierName + ": " + identifierValue, errorCode);
    }
}
