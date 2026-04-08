package com.example.userservice.service;

import com.example.userservice.dto.RegisterRequest;
import com.example.userservice.dto.UserProfileResponse;
import com.example.userservice.exceptions.UserAlreadyExistsWithEmailOrPhone;
import com.example.userservice.exceptions.UserNotFoundException;
import com.example.userservice.exceptions.UserRegistrationFailedException;
import com.example.userservice.exceptions.UserUpdateRequestFailedException;
import com.example.userservice.repository.UserDAOImpl;
import in.zeta.spectra.capture.SpectraLogger;
import lombok.RequiredArgsConstructor;
import olympus.trace.OlympusSpectra;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String ATTR_EMAIL = "email";
    private static final String ATTR_USER_ID = "userId";
    private static final SpectraLogger logger = OlympusSpectra.getLogger(UserService.class);

    private final UserDAOImpl userDAO;

    public CompletionStage<UserProfileResponse> registerUser(RegisterRequest registerRequest) {
        logger.info("Registering user").attr(ATTR_EMAIL, registerRequest.getEmail()).log();
        return userDAO.isPhoneOrEmailExists(registerRequest.getPhone(), registerRequest.getEmail())
                .thenCompose(exists -> {
                    if (exists) {
                        logger.warn("User already exists with email or phone").attr(ATTR_EMAIL, registerRequest.getEmail()).log();
                        return CompletableFuture.failedStage(
                                new UserAlreadyExistsWithEmailOrPhone("User with given email or phone number already exists."));
                    }

                    return userDAO.createUser(registerRequest)
                            .thenCompose(userProfile -> {
                                if (userProfile == null) {
                                    logger.error("User registration failed").attr(ATTR_EMAIL, registerRequest.getEmail()).log();
                                    return CompletableFuture.failedStage(
                                            new UserRegistrationFailedException("User Registration Failed"));
                                }
                                logger.info("User registered successfully").attr(ATTR_USER_ID, userProfile.getUserId()).log();
                                return CompletableFuture.completedFuture(userProfile);
                            });
                });
    }

    public CompletionStage<UserProfileResponse> getUserById(long userId) {
        logger.info("Fetching user by id").attr(ATTR_USER_ID, userId).log();
        return userDAO.getUserById(userId)
                .thenCompose(optionalUserProfile -> optionalUserProfile
                        .map(CompletableFuture::completedFuture)
                        .orElseGet(() -> {
                            logger.warn("User not found").attr(ATTR_USER_ID, userId).log();
                            return CompletableFuture.<UserProfileResponse>failedFuture(
                                    new UserNotFoundException("User not found with id: " + userId));
                        }));
    }

    public CompletionStage<UserProfileResponse> updateUser(long userId, RegisterRequest updateRequest) {
        logger.info("Updating user").attr(ATTR_USER_ID, userId).log();
        return userDAO.isPhoneOrEmailExistsExcludingUser(updateRequest.getPhone(), updateRequest.getEmail(), userId)
                .thenCompose(exists -> {
                    if (exists) {
                        logger.warn("User already exists with email or phone").attr(ATTR_EMAIL, updateRequest.getEmail()).log();
                        return CompletableFuture.failedStage(
                                new UserAlreadyExistsWithEmailOrPhone("User with given email or phone number already exists."));
                    }
                    return userDAO.updateUser(userId, updateRequest)
                            .thenCompose(userProfile -> {
                                if (userProfile == null) {
                                    logger.error("User update failed").attr(ATTR_USER_ID, userId).log();
                                    return CompletableFuture.failedStage(
                                            new UserUpdateRequestFailedException("User update request failed for id: " + userId));
                                }
                                logger.info("User updated successfully").attr(ATTR_USER_ID, userId).log();
                                return CompletableFuture.completedFuture(userProfile);
                            });
                });
    }

    public CompletionStage<UserProfileResponse> updateUserGlobalLimit(long userId, double newGlobalLimit) {
        logger.info("Updating user global limit").attr(ATTR_USER_ID, userId).attr("newLimit", newGlobalLimit).log();
        return getUserById(userId)
                .thenCompose(userProfile -> {
                    if (userProfile == null) {
                        logger.warn("User not found for global limit update").attr(ATTR_USER_ID, userId).log();
                        return CompletableFuture.failedStage(
                                new UserNotFoundException("User not found with id: " + userId));
                    }
                    return userDAO.updateUserGlobalLimit(userId, newGlobalLimit)
                            .thenApply(updatedUserProfile -> {
                                if (updatedUserProfile == null) {
                                    logger.error("Failed to update global limit").attr(ATTR_USER_ID, userId).log();
                                    throw new UserUpdateRequestFailedException("Failed to update global limit for user id: " + userId);
                                }
                                logger.info("Global limit updated successfully").attr(ATTR_USER_ID, userId).log();
                                return updatedUserProfile;
                            });
                });
    }
}


