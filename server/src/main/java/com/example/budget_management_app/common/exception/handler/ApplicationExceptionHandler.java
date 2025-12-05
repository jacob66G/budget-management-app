package com.example.budget_management_app.common.exception.handler;

import com.example.budget_management_app.common.exception.ApplicationException;
import com.example.budget_management_app.common.exception.ErrorCode;
import com.example.budget_management_app.common.exception.dto.ErrorResponse;
import com.example.budget_management_app.constants.ErrorConstants;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class ApplicationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Application exception: {}", ex.getMessage(), ex);
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        });

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "Request contains invalid fields",
                fieldErrors,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    //from security core
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        log.warn("Authentication exception occurred= {}, path= {}", ex.getMessage(), request.getRequestURI(), ex);

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "Authorization Failed",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {

        String errorMessage = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        log.warn("Parameter validation error. Message: {}, Path: {}",
                errorMessage, request.getRequestURI());

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ErrorConstants.PARAMETER_VALIDATION_ERROR,
                errorMessage,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        String errorMessage = getErrorMessage(ex);

        log.warn("Parameter type conversion error. Message: {}, Path: {}",
                errorMessage, request.getRequestURI());

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ErrorConstants.TYPE_MISMATCH_ERROR,
                errorMessage,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {

        String errorMessage = "Request contains invalid data format";
        String errorCode = ErrorConstants.INVALID_DATA_FORMAT;

        Throwable cause = ex.getCause();
        if (cause instanceof ValueInstantiationException && cause.getCause() instanceof ApplicationException) {
            ApplicationException applicationEx = (ApplicationException) cause.getCause();

            errorMessage = applicationEx.getMessage();
            errorCode = applicationEx.getErrorCode().name();
        }

        if (cause instanceof MismatchedInputException mismatchedInput) {

            if (mismatchedInput.getTargetType() != null && mismatchedInput.getTargetType().isEnum()) {

                String originalMessage = mismatchedInput.getOriginalMessage();
                try {
                    String incorrectValue = originalMessage.split("\"")[1];
                    errorMessage = String.format(
                            "Invalid value '%s'. Allowed values are: %s",
                            incorrectValue,
                            Arrays.toString(mismatchedInput.getTargetType().getEnumConstants())
                    );
                } catch (Exception e) {

                    log.warn("Could not parse bad enum value from exception message: {}", originalMessage, e);

                    String enumTypeName = mismatchedInput.getTargetType().getSimpleName();
                    errorMessage = String.format(
                            "Invalid value has been received for enum type %s. Allowed values are: %s",
                            enumTypeName,
                            Arrays.toString(mismatchedInput.getTargetType().getEnumConstants())
                    );
                }
            }
        }

        log.warn("Invalid request data format error. Message: {}, Path: {}", errorMessage, request.getRequestURI());

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                errorCode,
                errorMessage,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(ApplicationException ex, HttpServletRequest request) {
        log.error("Application exception: {}", ex.getMessage(), ex);
        ErrorCode errorCode = ex.getErrorCode();
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                errorCode.getStatus().value(),
                errorCode.name(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(errorCode.getStatus()).body(errorResponse);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpServletRequest request) {

        String paramName = ex.getParameterName();

        String errorMessage = String.format(
                "Required request parameter '%s' not found.",
                paramName
        );

        log.warn("Missing required request param error. Message: {}, Path: {}", errorMessage, request.getRequestURI());

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ErrorConstants.REQUIRED_PARAMETER_NOT_FOUND,
                errorMessage,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {

        log.error("Unhandled exception occurred: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ErrorConstants.INTERNAL_SERVER_ERROR,
                "An unexpected internal server error occurred.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    private static String getErrorMessage(MethodArgumentTypeMismatchException ex) {

        String paramName = ex.getName();
        String providedValue = String.valueOf(ex.getValue());
        Class<?> requiredType = ex.getRequiredType();

        String requiredTypeName = (requiredType != null) ? ex.getRequiredType().getSimpleName() : "unknown";

        String errorMessage;
        if (requiredType != null && requiredType.isEnum()) {

            String allowedValues = Arrays.stream(requiredType.getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));

            errorMessage = String.format(
                    "Invalid value '%s' for parameter '%s'. Allowed values are: [%s]",
                    providedValue,
                    paramName,
                    allowedValues
            );
        } else {

            errorMessage = String.format(
                    "Wrong format of '%s' parameter. Expected value of type '%s', but received '%s'",
                    paramName,
                    requiredTypeName,
                    providedValue
            );
        }
        return errorMessage;
    }
}
