package com.example.userservice.entity;

import com.example.userservice.model.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private UserStatus status;
    private Double globalCreditLimit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


