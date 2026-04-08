package com.example.userservice.exceptions;

public class UserAlreadyExistsWithEmailOrPhone extends UserServiceException {
    
    public UserAlreadyExistsWithEmailOrPhone(String details) {
        super(ErrorCode.USER_ALREADY_EXISTS, details);
    }

    public UserAlreadyExistsWithEmailOrPhone() {
        super(ErrorCode.USER_ALREADY_EXISTS);
    }
}

