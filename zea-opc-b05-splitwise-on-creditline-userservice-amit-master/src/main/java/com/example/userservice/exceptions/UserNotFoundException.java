package com.example.userservice.exceptions;

public class UserNotFoundException extends UserServiceException {
    
    public UserNotFoundException(String details) {
        super(ErrorCode.USER_NOT_FOUND, details);
    }

    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}

