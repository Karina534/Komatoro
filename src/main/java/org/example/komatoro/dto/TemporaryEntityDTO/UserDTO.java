package org.example.komatoro.dto.TemporaryEntityDTO;

import org.example.komatoro.model.Role;

import java.time.Instant;

public record UserDTO(
        Long id,
        String username,
        String email,
        Role role,
        Instant createdAt,
        boolean verified
) {

}
