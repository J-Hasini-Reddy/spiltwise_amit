package com.example.userservice.repository;

import com.example.userservice.dto.RegisterRequest;
import com.example.userservice.dto.UserProfileResponse;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface UserDAO {
    CompletionStage<UserProfileResponse> createUser(RegisterRequest registerUser);
    CompletionStage<Optional<UserProfileResponse>> getUserById(long userId);
    CompletionStage<UserProfileResponse> updateUser(long userId, RegisterRequest updateUser);
    CompletionStage<Boolean> isPhoneOrEmailExists(String phone, String email);
    CompletionStage<Boolean> isPhoneOrEmailExistsExcludingUser(String phone, String email, long userId);
    CompletionStage<UserProfileResponse> updateUserGlobalLimit(long userId, double newLimit);
}
