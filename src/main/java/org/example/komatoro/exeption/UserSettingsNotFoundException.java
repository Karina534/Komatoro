package org.example.komatoro.exeption;

import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

public class UserSettingsNotFoundException extends BusinessException{
    public UserSettingsNotFoundException(Long userId){
        super(
                "USER_SETTINGS_NOT_FOUND",
                String.format("User settings table with ID %s not found", userId),
                Map.of("userId", userId),
                HttpStatus.NOT_FOUND
        );
    }
}
