package com.example.userservice.exceptions;

import lombok.Getter;

@Getter
public class UserServiceException extends RuntimeException {
    
    private final ErrorCode errorCode;
    private final String details;

    public UserServiceException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = null;
    }

    public UserServiceException(ErrorCode errorCode, String details) {
        super(errorCode.getMessage() + ": " + details);
        this.errorCode = errorCode;
        this.details = details;
    }

    public UserServiceException(ErrorCode errorCode, String details, Throwable cause) {
        super(errorCode.getMessage() + ": " + details, cause);
        this.errorCode = errorCode;
        this.details = details;
    }

    public UserServiceException(String message) {
        super(message);
        this.errorCode = ErrorCode.INTERNAL_ERROR;
        this.details = message;
    }

    public UserServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.INTERNAL_ERROR;
        this.details = message;
    }
}
