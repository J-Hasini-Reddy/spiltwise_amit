package com.example.userservice.repository;

import com.example.userservice.dto.RegisterRequest;
import com.example.userservice.dto.UserProfileResponse;
import com.example.userservice.exceptions.UserNotFoundException;
import com.example.userservice.exceptions.UserUpdateRequestFailedException;
import in.zeta.springframework.boot.commons.postgres.GenericPostgresDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.RowMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDAOImplTest {

    @Mock
    private GenericPostgresDAO dao;

    private UserDAOImpl userDAOImpl;

    private RegisterRequest registerRequest;
    private UserProfileResponse userProfileResponse;

    @BeforeEach
    void setUp() {
        userDAOImpl = new UserDAOImpl(dao);

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
    @DisplayName("Create User Tests")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user successfully")
        @SuppressWarnings("unchecked")
        void createUser_Success() throws Exception {
            when(dao.queryForObject(anyString(), any(RowMapper.class), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(CompletableFuture.completedFuture(userProfileResponse));

            UserProfileResponse result = userDAOImpl.createUser(registerRequest).toCompletableFuture().get();

            assertNotNull(result);
            assertEquals(1L, result.getUserId());
            assertEquals("John Doe", result.getName());
        }

        @Test
        @DisplayName("Should handle database exception during create")
        @SuppressWarnings("unchecked")
        void createUser_DatabaseException() {
            when(dao.queryForObject(anyString(), any(RowMapper.class), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Database error")));

            CompletionException exception = assertThrows(CompletionException.class, () -> {
                var future = userDAOImpl.createUser(registerRequest).toCompletableFuture();
                future.join();
            });

            assertInstanceOf(RuntimeException.class, exception.getCause());
        }
    }

    @Nested
    @DisplayName("Get User By ID Tests")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should return user when found")
        @SuppressWarnings("unchecked")
        void getUserById_Found() throws Exception {
            when(dao.queryForOptionalObject(anyString(), any(RowMapper.class), eq(1L)))
                    .thenReturn(CompletableFuture.completedFuture(Optional.of(userProfileResponse)));

            Optional<UserProfileResponse> result = userDAOImpl.getUserById(1L).toCompletableFuture().get();

            assertTrue(result.isPresent());
            assertEquals(1L, result.get().getUserId());
        }

        @Test
        @DisplayName("Should return empty Optional when user not found")
        @SuppressWarnings("unchecked")
        void getUserById_NotFound() throws Exception {
            when(dao.queryForOptionalObject(anyString(), any(RowMapper.class), eq(999L)))
                    .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

            Optional<UserProfileResponse> result = userDAOImpl.getUserById(999L).toCompletableFuture().get();

            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("Update User Tests")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update user successfully")
        @SuppressWarnings("unchecked")
        void updateUser_Success() throws Exception {
            when(dao.query(anyString(), any(RowMapper.class), eq(1L)))
                    .thenReturn(CompletableFuture.completedFuture(List.of(userProfileResponse)));
            when(dao.update(anyString(), anyString(), anyString(), anyString(), anyDouble(), any(), eq(1L)))
                    .thenReturn(CompletableFuture.completedFuture(1));

            RegisterRequest updateRequest = new RegisterRequest();
            updateRequest.setName("Jane Doe");
            updateRequest.setEmail("jane@example.com");
            updateRequest.setPhone("0987654321");
            updateRequest.setGlobalCreditLimit(15000.00);

            UserProfileResponse result = userDAOImpl.updateUser(1L, updateRequest).toCompletableFuture().get();

            assertNotNull(result);
            assertEquals("Jane Doe", result.getName());
            assertEquals("jane@example.com", result.getEmail());
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user doesn't exist")
        @SuppressWarnings("unchecked")
        void updateUser_UserNotFound() {
            when(dao.query(anyString(), any(RowMapper.class), eq(999L)))
                    .thenReturn(CompletableFuture.completedFuture(Collections.emptyList()));

            CompletionException exception = assertThrows(CompletionException.class, () -> {
                var future = userDAOImpl.updateUser(999L, registerRequest).toCompletableFuture();
                future.join();
            });

            assertInstanceOf(UserNotFoundException.class, exception.getCause());
        }

        @Test
        @DisplayName("Should throw UserUpdateRequestFailedException when no rows affected")
        @SuppressWarnings("unchecked")
        void updateUser_NoRowsAffected() {
            when(dao.query(anyString(), any(RowMapper.class), eq(1L)))
                    .thenReturn(CompletableFuture.completedFuture(List.of(userProfileResponse)));
            when(dao.update(anyString(), anyString(), anyString(), anyString(), anyDouble(), any(), eq(1L)))
                    .thenReturn(CompletableFuture.completedFuture(0));

            CompletionException exception = assertThrows(CompletionException.class, () -> {
                var future = userDAOImpl.updateUser(1L, registerRequest).toCompletableFuture();
                future.join();
            });

            assertInstanceOf(UserUpdateRequestFailedException.class, exception.getCause());
        }
    }

    @Nested
    @DisplayName("Check Email/Phone Exists Tests")
    class CheckEmailPhoneExistsTests {

        @Test
        @DisplayName("Should return true when email or phone exists")
        @SuppressWarnings("unchecked")
        void isPhoneOrEmailExists_True() throws Exception {
            when(dao.queryForObject(anyString(), any(RowMapper.class), eq("1234567890"), eq("john@example.com")))
                    .thenReturn(CompletableFuture.completedFuture(true));

            Boolean result = userDAOImpl.isPhoneOrEmailExists("1234567890", "john@example.com")
                    .toCompletableFuture().get();

            assertTrue(result);
        }

        @Test
        @DisplayName("Should return false when email and phone don't exist")
        @SuppressWarnings("unchecked")
        void isPhoneOrEmailExists_False() throws Exception {
            when(dao.queryForObject(anyString(), any(RowMapper.class), eq("1234567890"), eq("john@example.com")))
                    .thenReturn(CompletableFuture.completedFuture(false));

            Boolean result = userDAOImpl.isPhoneOrEmailExists("1234567890", "john@example.com")
                    .toCompletableFuture().get();

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Check Email/Phone Exists Excluding User Tests")
    class CheckEmailPhoneExistsExcludingUserTests {

        @Test
        @DisplayName("Should return true when email/phone exists for another user")
        @SuppressWarnings("unchecked")
        void isPhoneOrEmailExistsExcludingUser_True() throws Exception {
            when(dao.queryForObject(anyString(), any(RowMapper.class), 
                    eq("1234567890"), eq("john@example.com"), eq(1L)))
                    .thenReturn(CompletableFuture.completedFuture(true));

            Boolean result = userDAOImpl.isPhoneOrEmailExistsExcludingUser("1234567890", "john@example.com", 1L)
                    .toCompletableFuture().get();

            assertTrue(result);
        }

        @Test
        @DisplayName("Should return false when email/phone only exists for the same user")
        @SuppressWarnings("unchecked")
        void isPhoneOrEmailExistsExcludingUser_False() throws Exception {
            when(dao.queryForObject(anyString(), any(RowMapper.class), 
                    eq("1234567890"), eq("john@example.com"), eq(1L)))
                    .thenReturn(CompletableFuture.completedFuture(false));

            Boolean result = userDAOImpl.isPhoneOrEmailExistsExcludingUser("1234567890", "john@example.com", 1L)
                    .toCompletableFuture().get();

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Update User Global Limit Tests")
    class UpdateUserGlobalLimitTests {

        @Test
        @DisplayName("Should update global limit successfully")
        @SuppressWarnings("unchecked")
        void updateUserGlobalLimit_Success() throws Exception {
            double newLimit = 25000.00;
            when(dao.query(anyString(), any(RowMapper.class), eq(1L)))
                    .thenReturn(CompletableFuture.completedFuture(List.of(userProfileResponse)));
            when(dao.update(anyString(), eq(newLimit), any(), eq(1L)))
                    .thenReturn(CompletableFuture.completedFuture(1));

            UserProfileResponse result = userDAOImpl.updateUserGlobalLimit(1L, newLimit)
                    .toCompletableFuture().get();

            assertNotNull(result);
            assertEquals(newLimit, result.getGlobalCreditLimit());
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user not found")
        @SuppressWarnings("unchecked")
        void updateUserGlobalLimit_UserNotFound() {
            when(dao.query(anyString(), any(RowMapper.class), eq(999L)))
                    .thenReturn(CompletableFuture.completedFuture(Collections.emptyList()));

            CompletionException exception = assertThrows(CompletionException.class, () -> {
                var future = userDAOImpl.updateUserGlobalLimit(999L, 20000.00).toCompletableFuture();
                future.join();
            });

            assertInstanceOf(UserNotFoundException.class, exception.getCause());
        }

        @Test
        @DisplayName("Should throw UserUpdateRequestFailedException when no rows affected")
        @SuppressWarnings("unchecked")
        void updateUserGlobalLimit_NoRowsAffected() {
            when(dao.query(anyString(), any(RowMapper.class), eq(1L)))
                    .thenReturn(CompletableFuture.completedFuture(List.of(userProfileResponse)));
            when(dao.update(anyString(), anyDouble(), any(), eq(1L)))
                    .thenReturn(CompletableFuture.completedFuture(0));

            CompletionException exception = assertThrows(CompletionException.class, () -> {
                var future = userDAOImpl.updateUserGlobalLimit(1L, 20000.00).toCompletableFuture();
                future.join();
            });

            assertInstanceOf(UserUpdateRequestFailedException.class, exception.getCause());
        }
    }
}
