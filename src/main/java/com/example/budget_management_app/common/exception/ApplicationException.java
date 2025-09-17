package com.example.budget_management_app.common.exception;

import lombok.Getter;

@Getter
public abstract class ApplicationException extends RuntimeException {
  private final ErrorCode errorCode;

  public ApplicationException(String message, ErrorCode errorCode, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
  }

  public ApplicationException(String message, ErrorCode errorCode) {
    super(message);
    this.errorCode = errorCode;
  }
}
