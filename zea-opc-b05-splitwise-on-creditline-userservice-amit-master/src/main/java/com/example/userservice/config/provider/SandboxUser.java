package com.example.userservice.config.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SandboxUser {
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String status;
    private Double globalCreditLimit;
}
