package com.example.userservice.exceptions;

public class UserRegistrationFailedException extends UserServiceException {
    
    public UserRegistrationFailedException(String details) {
        super(ErrorCode.USER_REGISTRATION_FAILED, details);
    }

    public UserRegistrationFailedException(String details, Throwable cause) {
        super(ErrorCode.USER_REGISTRATION_FAILED, details, cause);
    }
}

