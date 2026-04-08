package com.example.userservice.exceptions;

public class UserUpdateRequestFailedException extends UserServiceException {
    
    public UserUpdateRequestFailedException(String details) {
        super(ErrorCode.USER_UPDATE_FAILED, details);
    }

    public UserUpdateRequestFailedException(String details, Throwable cause) {
        super(ErrorCode.USER_UPDATE_FAILED, details, cause);
    }
}

