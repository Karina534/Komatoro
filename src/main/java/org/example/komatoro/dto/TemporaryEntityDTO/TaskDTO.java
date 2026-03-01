package org.example.komatoro.dto.TemporaryEntityDTO;

import java.time.Instant;
import java.util.UUID;

public record TaskDTO(
        UUID id,
        UUID userId,
        String title,
        String description,
        Boolean isActive,
        Instant createdAt
) {
}
