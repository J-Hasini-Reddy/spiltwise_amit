package com.example.userservice.service;

import com.example.userservice.dto.RegisterRequest;
import com.example.userservice.dto.UserProfileResponse;
import com.example.userservice.exceptions.UserAlreadyExistsWithEmailOrPhone;
import com.example.userservice.exceptions.UserNotFoundException;
import com.example.userservice.exceptions.UserRegistrationFailedException;
import com.example.userservice.exceptions.UserUpdateRequestFailedException;
import com.example.userservice.repository.UserDAOImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDAOImpl userDAO;

    @InjectMocks
    private UserService userService;

    private RegisterRequest registerRequest;
    private UserProfileResponse userProfileResponse;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setName("John Doe");
        registerRequest.setEmail("john@example.com");
        registerRequest.setPhone("1234567890");
        registerRequest.setGlobalCreditLimit(10000.00);

        userProfileResponse = new UserProfileResponse();
        userProfileResponse.setUserId(1L);
        userProfileResponse.setName("John Doe");
        userProfileResponse.setEmail("john@example.com");
        userProfileResponse.setPhone("1234567890");
        userProfileResponse.setGlobalCreditLimit(10000.00);
    }

    @Nested
    @DisplayName("Register User Tests")
    class RegisterUserTests {

        @Test
        @DisplayName("Should successfully register a new user")
        void registerUser_Success() throws Exception {
            // Given
            when(userDAO.isPhoneOrEmailExists(anyString(), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(false));
            when(userDAO.createUser(any(RegisterRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(userProfileResponse));

            // When
            UserProfileResponse result = userService.registerUser(registerRequest).toCompletableFuture().get();

            // Then
            assertNotNull(result);
            assertEquals(1L, result.getUserId());
            assertEquals("John Doe", result.getName());
            assertEquals("john@example.com", result.getEmail());
            verify(userDAO).isPhoneOrEmailExists("1234567890", "john@example.com");
            verify(userDAO).createUser(registerRequest);
        }

        @Test
        @DisplayName("Should throw UserAlreadyExistsWithEmailOrPhone when email or phone already exists")
        void registerUser_EmailOrPhoneExists() {
            // Given
            when(userDAO.isPhoneOrEmailExists(anyString(), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(true));

            // When & Then
            CompletionException exception = assertThrows(CompletionException.class, () ->
                    userService.registerUser(registerRequest).toCompletableFuture().join()
            );

            assertInstanceOf(UserAlreadyExistsWithEmailOrPhone.class, exception.getCause());
            verify(userDAO).isPhoneOrEmailExists("1234567890", "john@example.com");
            verify(userDAO, never()).createUser(any());
        }

        @Test
        @DisplayName("Should throw UserRegistrationFailedException when createUser returns null")
        void registerUser_CreateUserReturnsNull() {
            // Given
            when(userDAO.isPhoneOrEmailExists(anyString(), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(false));
            when(userDAO.createUser(any(RegisterRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(null));

            // When & Then
            CompletionException exception = assertThrows(CompletionException.class, () -> {
                var future = userService.registerUser(registerRequest).toCompletableFuture();
                future.join();
            });

            assertInstanceOf(UserRegistrationFailedException.class, exception.getCause());
            verify(userDAO).createUser(registerRequest);
        }

        @Test
        @DisplayName("Should handle DAO exception during email/phone check")
        void registerUser_DAOExceptionDuringCheck() {
            // Given
            RuntimeException daoException = new RuntimeException("Database error");
            when(userDAO.isPhoneOrEmailExists(anyString(), anyString()))
                    .thenReturn(CompletableFuture.failedFuture(daoException));

            // When & Then
            CompletionException exception = assertThrows(CompletionException.class, () -> {
                var future = userService.registerUser(registerRequest).toCompletableFuture();
                future.join();
            });

            assertEquals(daoException, exception.getCause());
        }
    }

    @Nested
    @DisplayName("Get User By ID Tests")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should return user when found")
        void getUserById_Success() throws Exception {
            // Given
            when(userDAO.getUserById(1L))
                    .thenReturn(CompletableFuture.completedFuture(Optional.of(userProfileResponse)));

            // When
            UserProfileResponse result = userService.getUserById(1L).toCompletableFuture().get();

            // Then
            assertNotNull(result);
            assertEquals(1L, result.getUserId());
            assertEquals("John Doe", result.getName());
            verify(userDAO).getUserById(1L);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user not found")
        void getUserById_NotFound() {
            // Given
            when(userDAO.getUserById(999L))
                    .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

            // When & Then
            CompletionException exception = assertThrows(CompletionException.class, () -> {
                var future = userService.getUserById(999L).toCompletableFuture();
                future.join();
            });

            assertInstanceOf(UserNotFoundException.class, exception.getCause());
            assertTrue(exception.getCause().getMessage().contains("999"));
        }

        @Test
        @DisplayName("Should handle DAO exception during fetch")
        void getUserById_DAOException() {
            // Given
            RuntimeException daoException = new RuntimeException("Database connection failed");
            when(userDAO.getUserById(anyLong()))
                    .thenReturn(CompletableFuture.failedFuture(daoException));

            // When & Then
            CompletionException exception = assertThrows(CompletionException.class, () -> {
                var future = userService.getUserById(1L).toCompletableFuture();
                future.join();
            });

            assertEquals(daoException, exception.getCause());
        }
    }

    @Nested
    @DisplayName("Update User Tests")
    class UpdateUserTests {

        @Test
        @DisplayName("Should successfully update user")
        void updateUser_Success() throws Exception {
            // Given
            RegisterRequest updateRequest = new RegisterRequest();
            updateRequest.setName("Jane Doe");
            updateRequest.setEmail("jane@example.com");
            updateRequest.setPhone("0987654321");
            updateRequest.setGlobalCreditLimit(15000.00);

            UserProfileResponse updatedResponse = new UserProfileResponse();
            updatedResponse.setUserId(1L);
            updatedResponse.setName("Jane Doe");
            updatedResponse.setEmail("jane@example.com");
            updatedResponse.setPhone("0987654321");
            updatedResponse.setGlobalCreditLimit(15000.00);

            when(userDAO.isPhoneOrEmailExistsExcludingUser(anyString(), anyString(), anyLong()))
                    .thenReturn(CompletableFuture.completedFuture(false));
            when(userDAO.updateUser(eq(1L), any(RegisterRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(updatedResponse));

            // When
            UserProfileResponse result = userService.updateUser(1L, updateRequest).toCompletableFuture().get();

            // Then
            assertNotNull(result);
            assertEquals("Jane Doe", result.getName());
            assertEquals("jane@example.com", result.getEmail());
            verify(userDAO).isPhoneOrEmailExistsExcludingUser("0987654321", "jane@example.com", 1L);
            verify(userDAO).updateUser(1L, updateRequest);
        }

        @Test
        @DisplayName("Should throw UserAlreadyExistsWithEmailOrPhone when email/phone conflicts with another user")
        void updateUser_EmailOrPhoneConflict() {
            // Given
            when(userDAO.isPhoneOrEmailExistsExcludingUser(anyString(), anyString(), anyLong()))
                    .thenReturn(CompletableFuture.completedFuture(true));

            // When & Then
            CompletionException exception = assertThrows(CompletionException.class, () -> {
                var future = userService.updateUser(1L, registerRequest).toCompletableFuture();
                future.join();
            });

            assertInstanceOf(UserAlreadyExistsWithEmailOrPhone.class, exception.getCause());
            verify(userDAO, never()).updateUser(anyLong(), any());
        }

        @Test
        @DisplayName("Should throw UserUpdateRequestFailedException when update returns null")
        void updateUser_UpdateReturnsNull() {
            // Given
            when(userDAO.isPhoneOrEmailExistsExcludingUser(anyString(), anyString(), anyLong()))
                    .thenReturn(CompletableFuture.completedFuture(false));
            when(userDAO.updateUser(anyLong(), any(RegisterRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(null));

            // When & Then
            CompletionException exception = assertThrows(CompletionException.class, () -> {
                var future = userService.updateUser(1L, registerRequest).toCompletableFuture();
                future.join();
            });

            assertInstanceOf(UserUpdateRequestFailedException.class, exception.getCause());
        }
    }

    @Nested
    @DisplayName("Update User Global Limit Tests")
    class UpdateUserGlobalLimitTests {

        @Test
        @DisplayName("Should successfully update user global limit")
        void updateUserGlobalLimit_Success() throws Exception {
            // Given
            double newLimit = 25000.00;
            UserProfileResponse updatedResponse = new UserProfileResponse();
            updatedResponse.setUserId(1L);
            updatedResponse.setName("John Doe");
            updatedResponse.setEmail("john@example.com");
            updatedResponse.setPhone("1234567890");
            updatedResponse.setGlobalCreditLimit(newLimit);

            when(userDAO.getUserById(1L))
                    .thenReturn(CompletableFuture.completedFuture(Optional.of(userProfileResponse)));
            when(userDAO.updateUserGlobalLimit(1L, newLimit))
                    .thenReturn(CompletableFuture.completedFuture(updatedResponse));

            // When
            UserProfileResponse result = userService.updateUserGlobalLimit(1L, newLimit).toCompletableFuture().get();

            // Then
            assertNotNull(result);
            assertEquals(newLimit, result.getGlobalCreditLimit());
            verify(userDAO).getUserById(1L);
            verify(userDAO).updateUserGlobalLimit(1L, newLimit);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user not found for global limit update")
        void updateUserGlobalLimit_UserNotFound() {
            // Given
            when(userDAO.getUserById(999L))
                    .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

            // When & Then
            CompletionException exception = assertThrows(CompletionException.class, () -> {
                var future = userService.updateUserGlobalLimit(999L, 20000.00).toCompletableFuture();
                future.join();
            });

            assertInstanceOf(UserNotFoundException.class, exception.getCause());
            verify(userDAO, never()).updateUserGlobalLimit(anyLong(), anyDouble());
        }

        @Test
        @DisplayName("Should throw UserUpdateRequestFailedException when global limit update returns null")
        void updateUserGlobalLimit_UpdateReturnsNull() {
            // Given
            when(userDAO.getUserById(1L))
                    .thenReturn(CompletableFuture.completedFuture(Optional.of(userProfileResponse)));
            when(userDAO.updateUserGlobalLimit(anyLong(), anyDouble()))
                    .thenReturn(CompletableFuture.completedFuture(null));

            // When & Then
            CompletionException exception = assertThrows(CompletionException.class, () -> {
                var future = userService.updateUserGlobalLimit(1L, 20000.00).toCompletableFuture();
                future.join();
            });

            assertInstanceOf(UserUpdateRequestFailedException.class, exception.getCause());
        }

        @Test
        @DisplayName("Should handle minimum valid global limit")
        void updateUserGlobalLimit_MinimumLimit() throws Exception {
            // Given
            double minLimit = 0.01;
            UserProfileResponse updatedResponse = new UserProfileResponse();
            updatedResponse.setUserId(1L);
            updatedResponse.setName("John Doe");
            updatedResponse.setEmail("john@example.com");
            updatedResponse.setPhone("1234567890");
            updatedResponse.setGlobalCreditLimit(minLimit);

            when(userDAO.getUserById(1L))
                    .thenReturn(CompletableFuture.completedFuture(Optional.of(userProfileResponse)));
            when(userDAO.updateUserGlobalLimit(1L, minLimit))
                    .thenReturn(CompletableFuture.completedFuture(updatedResponse));

            // When
            UserProfileResponse result = userService.updateUserGlobalLimit(1L, minLimit).toCompletableFuture().get();

            // Then
            assertEquals(minLimit, result.getGlobalCreditLimit());
        }

        @Test
        @DisplayName("Should handle large global limit values")
        void updateUserGlobalLimit_LargeLimit() throws Exception {
            // Given
            double largeLimit = 9999999999.99;
            UserProfileResponse updatedResponse = new UserProfileResponse();
            updatedResponse.setUserId(1L);
            updatedResponse.setName("John Doe");
            updatedResponse.setEmail("john@example.com");
            updatedResponse.setPhone("1234567890");
            updatedResponse.setGlobalCreditLimit(largeLimit);

            when(userDAO.getUserById(1L))
                    .thenReturn(CompletableFuture.completedFuture(Optional.of(userProfileResponse)));
            when(userDAO.updateUserGlobalLimit(1L, largeLimit))
                    .thenReturn(CompletableFuture.completedFuture(updatedResponse));

            // When
            UserProfileResponse result = userService.updateUserGlobalLimit(1L, largeLimit).toCompletableFuture().get();

            // Then
            assertEquals(largeLimit, result.getGlobalCreditLimit());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle user with zero global credit limit")
        void registerUser_ZeroGlobalLimit() throws Exception {
            // Given
            registerRequest.setGlobalCreditLimit(0.00);
            userProfileResponse.setGlobalCreditLimit(0.00);

            when(userDAO.isPhoneOrEmailExists(anyString(), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(false));
            when(userDAO.createUser(any(RegisterRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(userProfileResponse));

            // When
            UserProfileResponse result = userService.registerUser(registerRequest).toCompletableFuture().get();

            // Then
            assertEquals(0.00, result.getGlobalCreditLimit());
        }

        @Test
        @DisplayName("Should handle user with maximum length name")
        void registerUser_MaxLengthName() throws Exception {
            // Given
            String maxLengthName = "A".repeat(255);
            registerRequest.setName(maxLengthName);
            userProfileResponse.setName(maxLengthName);

            when(userDAO.isPhoneOrEmailExists(anyString(), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(false));
            when(userDAO.createUser(any(RegisterRequest.class)))
                    .thenReturn(CompletableFuture.completedFuture(userProfileResponse));

            // When
            UserProfileResponse result = userService.registerUser(registerRequest).toCompletableFuture().get();

            // Then
            assertEquals(maxLengthName, result.getName());
        }

        @Test
        @DisplayName("Should handle concurrent registration attempts")
        void registerUser_ConcurrentAttempts() {
            // Given - simulate race condition where email check passes but insert fails
            when(userDAO.isPhoneOrEmailExists(anyString(), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(false));
            when(userDAO.createUser(any(RegisterRequest.class)))
                    .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Duplicate key violation")));

            // When & Then
            CompletionException exception = assertThrows(CompletionException.class, () ->
                    userService.registerUser(registerRequest).toCompletableFuture().join()
            );

            assertInstanceOf(RuntimeException.class, exception.getCause());
            assertTrue(exception.getCause().getMessage().contains("Duplicate key"));
        }
    }
}
