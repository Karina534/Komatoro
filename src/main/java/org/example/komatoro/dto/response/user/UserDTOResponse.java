package org.example.komatoro.dto.response.user;

import org.example.komatoro.model.Role;

import java.time.Instant;

/**
 * DTO ответа пользователя
 * @param id
 * @param username
 * @param email
 * @param role
 * @param createdAt
 * @param verified
 */
public record UserDTOResponse(
        Long id,
        String username,
        String email,
        Role role,
        Instant createdAt,
        boolean verified
) {
}
