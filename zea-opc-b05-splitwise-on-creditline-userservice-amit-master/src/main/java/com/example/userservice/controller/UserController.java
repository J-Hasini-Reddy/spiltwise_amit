package com.example.userservice.controller;

import com.example.userservice.config.provider.UserProvider;
import com.example.userservice.dto.RegisterRequest;
import com.example.userservice.dto.UserProfileResponse;
import com.example.userservice.service.UserService;
import in.zeta.spectra.capture.SpectraLogger;
import in.zeta.springframework.boot.commons.annotations.Authenticated;
import in.zeta.springframework.boot.commons.authorization.sandboxAccessControl.SandboxAuthorizedSync;
import lombok.RequiredArgsConstructor;
import olympus.trace.OlympusSpectra;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletionStage;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private static final SpectraLogger logger = OlympusSpectra.getLogger(UserController.class);
    private static final String ATTR_USER_ID = "userId";

    private final UserService userService;

    @PostMapping("/register")
    public CompletionStage<ResponseEntity<UserProfileResponse>> register(@Validated @RequestBody RegisterRequest request) {
        logger.info("Registering new user").attr("email", request.getEmail()).log();
        return userService.registerUser(request)
                .thenApply(user -> {
                    logger.info("User registered successfully").attr(ATTR_USER_ID, user.getUserId()).log();
                    return ResponseEntity.status(HttpStatus.CREATED).body(user);
                });
    }

    @GetMapping("/profile/userId/{userId}")
    public CompletionStage<ResponseEntity<UserProfileResponse>> getUserProfile(@PathVariable("userId") long userId) {
        logger.info("Fetching user profile").attr(ATTR_USER_ID, userId).log();
        return userService.getUserById(userId)
                .thenApply(ResponseEntity::ok);
    }

    @PutMapping("/update/userId/{userId}")
    @Authenticated
    @SandboxAuthorizedSync(action = "user.update", object = "$$userId$$@" + UserProvider.OBJECT_TYPE + ".cipher.app", tenantID = "1001034")
    public CompletionStage<ResponseEntity<UserProfileResponse>> updateUser(@Validated @RequestBody RegisterRequest request, @PathVariable("userId") long userId) {
        logger.info("Updating user").attr(ATTR_USER_ID, userId).log();
        return userService.updateUser(userId, request)
                .thenApply(updatedProfile -> {
                    logger.info("User updated successfully").attr(ATTR_USER_ID, userId).log();
                    return ResponseEntity.ok(updatedProfile);
                });
    }

    @PutMapping("/update/{userId}/globalCreditLimit/{newLimit}")
    public CompletionStage<ResponseEntity<UserProfileResponse>> updateGlobalCreditLimit(@PathVariable("userId") long userId, @PathVariable("newLimit") double newLimit) {
        logger.info("Updating user global credit limit").attr(ATTR_USER_ID, userId).attr("newLimit", newLimit).log();
        return userService.updateUserGlobalLimit(userId, newLimit)
                .thenApply(updatedProfile -> {
                    logger.info("User global credit limit updated").attr(ATTR_USER_ID, userId).log();
                    return ResponseEntity.ok(updatedProfile);
                });
    }
}


