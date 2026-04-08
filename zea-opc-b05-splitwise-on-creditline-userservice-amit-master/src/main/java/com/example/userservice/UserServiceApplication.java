package com.example.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.example.userservice",
        "in.zeta.springframework.boot.commons",
        "tech.zeta.academy.olympus.cipher"
})
public class UserServiceApplication {

    private UserServiceApplication() {
    }

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
