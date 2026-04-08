package com.example.userservice.controller;

import com.example.userservice.dto.RegisterRequest;
import com.example.userservice.dto.UserProfileResponse;
import com.example.userservice.exceptions.UserAlreadyExistsWithEmailOrPhone;
import com.example.userservice.exceptions.UserNotFoundException;
import com.example.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private RegisterRequest validRequest;
    private UserProfileResponse userResponse;

    @BeforeEach
    void setUp() {
        validRequest = new RegisterRequest();
        validRequest.setName("John Doe");
        validRequest.setEmail("john@example.com");
        validRequest.setPhone("1234567890");
        validRequest.setGlobalCreditLimit(10000.00);

        userResponse = new UserProfileResponse();
        userResponse.setUserId(1L);
        userResponse.setName("John Doe");
        userResponse.setEmail("john@example.com");
        userResponse.setPhone("1234567890");
        userResponse.setGlobalCreditLimit(10000.00);
    }

    @Nested
    @DisplayName("Register User Tests")
    class RegisterUserTests {

        @Test
        @DisplayName("Should register user successfully and return 201 CREATED")
        void register_Success() throws Exception {
            when(userService.registerUser(any(RegisterRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(userResponse));

            ResponseEntity<UserProfileResponse> response = userController.register(validRequest)
                    .toCompletableFuture().get();

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1L, response.getBody().getUserId());
            assertEquals("John Doe", response.getBody().getName());
            verify(userService).registerUser(validRequest);
        }

        @Test
        @DisplayName("Should throw exception when email or phone already exists")
        void register_DuplicateEmailOrPhone() {
            when(userService.registerUser(any(RegisterRequest.class)))
                    .thenReturn(CompletableFuture.failedFuture(
                            new UserAlreadyExistsWithEmailOrPhone("User with given email or phone number already exists.")));

            CompletionException exception = assertThrows(CompletionException.class, () -> {
                var future = userController.register(validRequest).toCompletableFuture();
                future.join();
            });

            assertInstanceOf(UserAlreadyExistsWithEmailOrPhone.class, exception.getCause());
        }
    }

    @Nested
    @DisplayName("Get User Profile Tests")
    class GetUserProfileTests {

        @Test
        @DisplayName("Should return user profile successfully")
        void getUserProfile_Success() throws Exception {
            when(userService.getUserById(1L))
                    .thenReturn(CompletableFuture.completedFuture(userResponse));

            ResponseEntity<UserProfileResponse> response = userController.getUserProfile(1L)
                    .toCompletableFuture().get();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1L, response.getBody().getUserId());
            verify(userService).getUserById(1L);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user not found")
        void getUserProfile_NotFound() {
            when(userService.getUserById(999L))
                    .thenReturn(CompletableFuture.failedFuture(
                            new UserNotFoundException("User not found with id: 999")));

            CompletionException exception = assertThrows(CompletionException.class, () -> {
                var future = userController.getUserProfile(999L).toCompletableFuture();
                future.join();
            });

            assertInstanceOf(UserNotFoundException.class, exception.getCause());
        }

        @Test
        @DisplayName("Should handle large user IDs")
        void getUserProfile_LargeUserId() throws Exception {
            long largeId = 9999999999L;
            userResponse.setUserId(largeId);
            when(userService.getUserById(largeId))
                    .thenReturn(CompletableFuture.completedFuture(userResponse));

            ResponseEntity<UserProfileResponse> response = userController.getUserProfile(largeId)
                    .toCompletableFuture().get();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(largeId, response.getBody().getUserId());
        }
    }

    @Nested
    @DisplayName("Update User Tests")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update user successfully")
        void updateUser_Success() throws Exception {
            UserProfileResponse updatedResponse = new UserProfileResponse();
            updatedResponse.setUserId(1L);
            updatedResponse.setName("Jane Doe");
            updatedResponse.setEmail("jane@example.com");
            updatedResponse.setPhone("0987654321");
            updatedResponse.setGlobalCreditLimit(15000.00);

            RegisterRequest updateRequest = new RegisterRequest();
            updateRequest.setName("Jane Doe");
            updateRequest.setEmail("jane@example.com");
            updateRequest.setPhone("0987654321");
            updateRequest.setGlobalCreditLimit(15000.00);

            when(userService.updateUser(eq(1L), any(RegisterRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(updatedResponse));

            ResponseEntity<UserProfileResponse> response = userController.updateUser(updateRequest, 1L)
                    .toCompletableFuture().get();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Jane Doe", response.getBody().getName());
            verify(userService).updateUser(eq(1L), any(RegisterRequest.class));
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when updating non-existent user")
        void updateUser_UserNotFound() {
            when(userService.updateUser(eq(999L), any(RegisterRequest.class)))
                    .thenReturn(CompletableFuture.failedFuture(
                            new UserNotFoundException("User not found with id: 999")));

            CompletionException exception = assertThrows(CompletionException.class, () -> {
                var future = userController.updateUser(validRequest, 999L).toCompletableFuture();
                future.join();
            });

            assertInstanceOf(UserNotFoundException.class, exception.getCause());
        }
    }

    @Nested
    @DisplayName("Update Global Credit Limit Tests")
    class UpdateGlobalCreditLimitTests {

        @Test
        @DisplayName("Should update global credit limit successfully")
        void updateGlobalCreditLimit_Success() throws Exception {
            double newLimit = 25000.00;
            UserProfileResponse updatedResponse = new UserProfileResponse();
            updatedResponse.setUserId(1L);
            updatedResponse.setName("John Doe");
            updatedResponse.setEmail("john@example.com");
            updatedResponse.setPhone("1234567890");
            updatedResponse.setGlobalCreditLimit(newLimit);

            when(userService.updateUserGlobalLimit(1L, newLimit))
                    .thenReturn(CompletableFuture.completedFuture(updatedResponse));

            ResponseEntity<UserProfileResponse> response = userController.updateGlobalCreditLimit(1L, newLimit)
                    .toCompletableFuture().get();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(newLimit, response.getBody().getGlobalCreditLimit());
            verify(userService).updateUserGlobalLimit(1L, newLimit);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user not found")
        void updateGlobalCreditLimit_UserNotFound() {
            when(userService.updateUserGlobalLimit(eq(999L), anyDouble()))
                    .thenReturn(CompletableFuture.failedFuture(
                            new UserNotFoundException("User not found with id: 999")));

            CompletionException exception = assertThrows(CompletionException.class, () -> {
                var future = userController.updateGlobalCreditLimit(999L, 10000.00).toCompletableFuture();
                future.join();
            });

            assertInstanceOf(UserNotFoundException.class, exception.getCause());
        }

        @Test
        @DisplayName("Should handle decimal credit limit values")
        void updateGlobalCreditLimit_DecimalValue() throws Exception {
            double decimalLimit = 12345.67;
            UserProfileResponse updatedResponse = new UserProfileResponse();
            updatedResponse.setUserId(1L);
            updatedResponse.setName("John Doe");
            updatedResponse.setEmail("john@example.com");
            updatedResponse.setPhone("1234567890");
            updatedResponse.setGlobalCreditLimit(decimalLimit);

            when(userService.updateUserGlobalLimit(1L, decimalLimit))
                    .thenReturn(CompletableFuture.completedFuture(updatedResponse));

            ResponseEntity<UserProfileResponse> response = userController.updateGlobalCreditLimit(1L, decimalLimit)
                    .toCompletableFuture().get();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(decimalLimit, response.getBody().getGlobalCreditLimit());
        }

        @Test
        @DisplayName("Should handle zero credit limit")
        void updateGlobalCreditLimit_ZeroValue() throws Exception {
            double zeroLimit = 0.0;
            UserProfileResponse updatedResponse = new UserProfileResponse();
            updatedResponse.setUserId(1L);
            updatedResponse.setName("John Doe");
            updatedResponse.setEmail("john@example.com");
            updatedResponse.setPhone("1234567890");
            updatedResponse.setGlobalCreditLimit(zeroLimit);

            when(userService.updateUserGlobalLimit(1L, zeroLimit))
                    .thenReturn(CompletableFuture.completedFuture(updatedResponse));

            ResponseEntity<UserProfileResponse> response = userController.updateGlobalCreditLimit(1L, zeroLimit)
                    .toCompletableFuture().get();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(zeroLimit, response.getBody().getGlobalCreditLimit());
        }
    }
}
