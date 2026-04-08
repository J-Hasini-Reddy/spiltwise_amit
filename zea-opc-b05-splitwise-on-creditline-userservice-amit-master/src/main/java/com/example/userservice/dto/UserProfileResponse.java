package com.example.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private long userId;
    private String name;
    private String email;
    private String phone;
    private double globalCreditLimit;
}

