package com.example.budget_management_app.common.exception;

public class StatusAlreadySetException extends ApplicationException{

    public StatusAlreadySetException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

    public StatusAlreadySetException(String resource, Long id, String statusName, ErrorCode errorCode) {
        super(resource + " with id: " + id + " is already in " + statusName + " state", errorCode);
    }
}
