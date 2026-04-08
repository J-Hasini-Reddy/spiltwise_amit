package com.example.userservice.exceptions;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    
    // Authentication & Authorization errors
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Authentication required. Please provide a valid auth token."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Access denied. You do not have permission to access this resource."),
    INVALID_AUTH_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid or expired authentication token."),
    
    // User related errors
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User does not exist"),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "User already exists with given email or phone"),
    USER_REGISTRATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to register user"),
    USER_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update user"),
    
    // Request validation errors
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "Invalid request parameters"),
    
    // System errors
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected internal error occurred"),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Database operation failed");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public String getCode() {
        return String.valueOf(httpStatus.value());
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }
}
