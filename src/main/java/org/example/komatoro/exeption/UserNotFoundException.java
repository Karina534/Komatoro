package org.example.komatoro.exeption;

import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

public class UserNotFoundException extends BusinessException{

    public UserNotFoundException(Long userId) {
        super(
                "USER_NOT_FOUND",
                String.format("User with ID %s not found", userId),
                Map.of("userId", userId),
                HttpStatus.NOT_FOUND
        );
    }

    public UserNotFoundException(String email){
        super(
                "USER_NOT_FOUND_BY_EMAIL",
                String.format("User with email %s not found", email),
                Map.of("email", email),
                HttpStatus.NOT_FOUND
        );
    }
}
