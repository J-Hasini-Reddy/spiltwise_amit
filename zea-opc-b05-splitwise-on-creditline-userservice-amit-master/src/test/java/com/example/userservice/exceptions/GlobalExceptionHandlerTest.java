package com.example.userservice.exceptions;

import com.example.userservice.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        lenient().when(request.getRequestURI()).thenReturn("/api/v1/user/test");
        lenient().when(request.getMethod()).thenReturn("POST");
    }

    @Nested
    @DisplayName("UserServiceException Handler Tests")
    class UserServiceExceptionTests {

        @Test
        @DisplayName("Should handle UserNotFoundException correctly")
        void handleUserNotFoundException() {
            UserNotFoundException exception = new UserNotFoundException("User not found with id: 123");

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleUserServiceException(exception, request);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Not Found", response.getBody().getError());
            assertEquals(404, response.getBody().getStatus());
        }

        @Test
        @DisplayName("Should handle UserAlreadyExistsWithEmailOrPhone correctly")
        void handleUserAlreadyExistsWithEmailOrPhone() {
            UserAlreadyExistsWithEmailOrPhone exception = 
                    new UserAlreadyExistsWithEmailOrPhone("User with given email or phone number already exists.");

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleUserServiceException(exception, request);

            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Conflict", response.getBody().getError());
            assertEquals(409, response.getBody().getStatus());
        }

        @Test
        @DisplayName("Should handle UserRegistrationFailedException correctly")
        void handleUserRegistrationFailedException() {
            UserRegistrationFailedException exception = 
                    new UserRegistrationFailedException("User Registration Failed");

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleUserServiceException(exception, request);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Internal Server Error", response.getBody().getError());
            assertEquals(500, response.getBody().getStatus());
        }

        @Test
        @DisplayName("Should handle UserUpdateRequestFailedException correctly")
        void handleUserUpdateRequestFailedException() {
            UserUpdateRequestFailedException exception = 
                    new UserUpdateRequestFailedException("User update request failed for id: 123");

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleUserServiceException(exception, request);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Internal Server Error", response.getBody().getError());
            assertEquals(500, response.getBody().getStatus());
        }
    }

    @Nested
    @DisplayName("CompletionException Handler Tests")
    class CompletionExceptionTests {

        @Test
        @DisplayName("Should unwrap UserServiceException from CompletionException")
        void handleCompletionException_WithUserServiceException() {
            UserNotFoundException cause = new UserNotFoundException("User not found");
            CompletionException exception = new CompletionException(cause);

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleCompletionException(exception, request);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        @DisplayName("Should handle CompletionException with unknown cause")
        void handleCompletionException_WithUnknownCause() {
            RuntimeException cause = new RuntimeException("Unknown error");
            CompletionException exception = new CompletionException(cause);

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleCompletionException(exception, request);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().getMessage().contains("Unknown error"));
        }

        @Test
        @DisplayName("Should handle CompletionException with null cause")
        void handleCompletionException_WithNullCause() {
            CompletionException exception = new CompletionException("Direct message", null);

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleCompletionException(exception, request);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("Validation Exception Handler Tests")
    class ValidationExceptionTests {

        @Test
        @DisplayName("Should handle MethodArgumentNotValidException with field errors")
        void handleValidationErrors_WithFieldErrors() {
            MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
            BindingResult bindingResult = mock(BindingResult.class);
            
            FieldError fieldError1 = new FieldError("registerRequest", "email", "Email should be valid");
            FieldError fieldError2 = new FieldError("registerRequest", "name", "Name is required");
            
            when(exception.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationErrors(exception, request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().getMessage().contains("email"));
            assertTrue(response.getBody().getMessage().contains("name"));
        }

        @Test
        @DisplayName("Should handle MethodArgumentNotValidException with empty errors")
        void handleValidationErrors_WithEmptyErrors() {
            MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
            BindingResult bindingResult = mock(BindingResult.class);
            
            when(exception.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getFieldErrors()).thenReturn(List.of());

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationErrors(exception, request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("ResponseStatusException Handler Tests")
    class ResponseStatusExceptionTests {

        @Test
        @DisplayName("Should handle ResponseStatusException with request")
        void handleResponseStatusException_WithRequest() {
            ResponseStatusException exception = new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad request reason");

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleResponseStatusException(exception, request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Bad request reason", response.getBody().getMessage());
        }

        @Test
        @DisplayName("Should handle ResponseStatusException without request")
        void handleResponseStatusException_WithoutRequest() {
            ResponseStatusException exception = new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleResponseStatusException(exception);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Resource not found", response.getBody().getMessage());
        }
    }

    @Nested
    @DisplayName("Generic Exception Handler Tests")
    class GenericExceptionTests {

        @Test
        @DisplayName("Should handle generic Exception")
        void handleGenericException() {
            Exception exception = new Exception("Unexpected error occurred");

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception, request);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Internal Server Error", response.getBody().getError());
            assertEquals(500, response.getBody().getStatus());
        }

        @Test
        @DisplayName("Should handle NullPointerException")
        void handleNullPointerException() {
            NullPointerException exception = new NullPointerException("Null value encountered");

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception, request);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        }

        @Test
        @DisplayName("Should handle IllegalArgumentException")
        void handleIllegalArgumentException() {
            IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception, request);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        }
    }
}
