package org.example.komatoro.dto.request.user;

import jakarta.validation.constraints.*;
import org.example.komatoro.model.Role;

/**
 * DTO запроса для создания пользователя
 * @param username
 * @param password
 * @param email
 */
public record UserCreateDTORequest(
        @NotBlank(message = "Username is required")
        @Size(min = 2, max = 50, message = "Username must be between {min} and {max} characters")
        @Pattern(regexp = "^[a-zA-Zа-яА-Я0-9_-]+$", message = "Username can only contains letters, numbers," +
                "underscores and hyphens")
        String username,

        //TODO: Добавить pattern ограничение на пароль
        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 50, message = "password must be between {min} and {max} characters")
        String password,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotNull(message = "Role is required")
        Role role
) {
}
