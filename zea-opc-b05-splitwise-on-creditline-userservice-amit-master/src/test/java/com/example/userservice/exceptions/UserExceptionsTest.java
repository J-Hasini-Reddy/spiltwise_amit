package com.example.userservice.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserExceptionsTest {

    @Nested
    @DisplayName("UserNotFoundException Tests")
    class UserNotFoundExceptionTests {

        @Test
        @DisplayName("Should create exception with message")
        void createWithMessage() {
            String message = "User not found with id: 123";
            UserNotFoundException exception = new UserNotFoundException(message);

            assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
            assertEquals(message, exception.getDetails());
            assertNotNull(exception.getMessage());
        }

        @Test
        @DisplayName("Should be an instance of UserServiceException")
        void isUserServiceException() {
            UserNotFoundException exception = new UserNotFoundException("test");
            
            assertInstanceOf(UserServiceException.class, exception);
        }
    }

    @Nested
    @DisplayName("UserAlreadyExistsWithEmailOrPhone Tests")
    class UserAlreadyExistsWithEmailOrPhoneTests {

        @Test
        @DisplayName("Should create exception with message")
        void createWithMessage() {
            String message = "User with given email or phone number already exists.";
            UserAlreadyExistsWithEmailOrPhone exception = new UserAlreadyExistsWithEmailOrPhone(message);

            assertEquals(ErrorCode.USER_ALREADY_EXISTS, exception.getErrorCode());
            assertEquals(message, exception.getDetails());
        }

        @Test
        @DisplayName("Should be an instance of UserServiceException")
        void isUserServiceException() {
            UserAlreadyExistsWithEmailOrPhone exception = new UserAlreadyExistsWithEmailOrPhone("test");
            
            assertInstanceOf(UserServiceException.class, exception);
        }
    }

    @Nested
    @DisplayName("UserRegistrationFailedException Tests")
    class UserRegistrationFailedExceptionTests {

        @Test
        @DisplayName("Should create exception with message")
        void createWithMessage() {
            String message = "User Registration Failed";
            UserRegistrationFailedException exception = new UserRegistrationFailedException(message);

            assertEquals(ErrorCode.USER_REGISTRATION_FAILED, exception.getErrorCode());
            assertEquals(message, exception.getDetails());
        }

        @Test
        @DisplayName("Should be an instance of UserServiceException")
        void isUserServiceException() {
            UserRegistrationFailedException exception = new UserRegistrationFailedException("test");
            
            assertInstanceOf(UserServiceException.class, exception);
        }
    }

    @Nested
    @DisplayName("UserUpdateRequestFailedException Tests")
    class UserUpdateRequestFailedExceptionTests {

        @Test
        @DisplayName("Should create exception with message")
        void createWithMessage() {
            String message = "User update request failed for id: 123";
            UserUpdateRequestFailedException exception = new UserUpdateRequestFailedException(message);

            assertEquals(ErrorCode.USER_UPDATE_FAILED, exception.getErrorCode());
            assertEquals(message, exception.getDetails());
        }

        @Test
        @DisplayName("Should be an instance of UserServiceException")
        void isUserServiceException() {
            UserUpdateRequestFailedException exception = new UserUpdateRequestFailedException("test");
            
            assertInstanceOf(UserServiceException.class, exception);
        }
    }

    @Nested
    @DisplayName("UserServiceException Tests")
    class UserServiceExceptionTests {

        @Test
        @DisplayName("Should create exception with errorCode and details")
        void createWithErrorCodeAndDetails() {
            String details = "Some error details";
            UserNotFoundException exception = new UserNotFoundException(details);

            assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
            assertEquals(details, exception.getDetails());
            assertTrue(exception.getMessage().contains(ErrorCode.USER_NOT_FOUND.getMessage()));
        }

        @Test
        @DisplayName("Should be an instance of RuntimeException")
        void isRuntimeException() {
            UserNotFoundException exception = new UserNotFoundException("test");
            
            assertInstanceOf(RuntimeException.class, exception);
        }
    }

    @Nested
    @DisplayName("ErrorCode Tests")
    class ErrorCodeTests {

        @Test
        @DisplayName("USER_NOT_FOUND should have correct values")
        void userNotFound() {
            assertEquals("404", ErrorCode.USER_NOT_FOUND.getCode());
            assertEquals("User does not exist", ErrorCode.USER_NOT_FOUND.getMessage());
            assertEquals(org.springframework.http.HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND.getHttpStatus());
        }

        @Test
        @DisplayName("USER_ALREADY_EXISTS should have correct values")
        void userAlreadyExists() {
            assertEquals("409", ErrorCode.USER_ALREADY_EXISTS.getCode());
            assertEquals("User already exists with given email or phone", ErrorCode.USER_ALREADY_EXISTS.getMessage());
            assertEquals(org.springframework.http.HttpStatus.CONFLICT, ErrorCode.USER_ALREADY_EXISTS.getHttpStatus());
        }

        @Test
        @DisplayName("USER_REGISTRATION_FAILED should have correct values")
        void userRegistrationFailed() {
            assertEquals("500", ErrorCode.USER_REGISTRATION_FAILED.getCode());
            assertEquals("Failed to register user", ErrorCode.USER_REGISTRATION_FAILED.getMessage());
            assertEquals(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.USER_REGISTRATION_FAILED.getHttpStatus());
        }

        @Test
        @DisplayName("USER_UPDATE_FAILED should have correct values")
        void userUpdateFailed() {
            assertEquals("500", ErrorCode.USER_UPDATE_FAILED.getCode());
            assertEquals("Failed to update user", ErrorCode.USER_UPDATE_FAILED.getMessage());
            assertEquals(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.USER_UPDATE_FAILED.getHttpStatus());
        }

        @Test
        @DisplayName("INVALID_REQUEST should have correct values")
        void invalidRequest() {
            assertEquals("400", ErrorCode.INVALID_REQUEST.getCode());
            assertEquals("Invalid request parameters", ErrorCode.INVALID_REQUEST.getMessage());
            assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST.getHttpStatus());
        }

        @Test
        @DisplayName("INTERNAL_ERROR should have correct values")
        void internalError() {
            assertEquals("500", ErrorCode.INTERNAL_ERROR.getCode());
            assertEquals("An unexpected internal error occurred", ErrorCode.INTERNAL_ERROR.getMessage());
            assertEquals(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR.getHttpStatus());
        }

        @Test
        @DisplayName("UNAUTHORIZED should have correct values")
        void unauthorized() {
            assertEquals("401", ErrorCode.UNAUTHORIZED.getCode());
            assertEquals("Authentication required. Please provide a valid auth token.", ErrorCode.UNAUTHORIZED.getMessage());
            assertEquals(org.springframework.http.HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.getHttpStatus());
        }

        @Test
        @DisplayName("FORBIDDEN should have correct values")
        void forbidden() {
            assertEquals("403", ErrorCode.FORBIDDEN.getCode());
            assertEquals("Access denied. You do not have permission to access this resource.", ErrorCode.FORBIDDEN.getMessage());
            assertEquals(org.springframework.http.HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN.getHttpStatus());
        }

        @Test
        @DisplayName("INVALID_AUTH_TOKEN should have correct values")
        void invalidAuthToken() {
            assertEquals("401", ErrorCode.INVALID_AUTH_TOKEN.getCode());
            assertEquals("Invalid or expired authentication token.", ErrorCode.INVALID_AUTH_TOKEN.getMessage());
            assertEquals(org.springframework.http.HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_AUTH_TOKEN.getHttpStatus());
        }
    }
}
