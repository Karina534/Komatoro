package org.example.komatoro.security.jwt;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO для хранения информации о JWT токене. Содержит идентификатор токена,
 * субъект (имя пользователя), список ролей (authorities), время создания и время истечения срока действия токена.
 * @param id
 * @param subject
 * @param authorities
 * @param createdAt
 * @param expiresAt
 */
public record JwtToken(
        UUID id,
        String subject,
        List<String> authorities,
        Instant createdAt,
        Instant expiresAt
) {
}
