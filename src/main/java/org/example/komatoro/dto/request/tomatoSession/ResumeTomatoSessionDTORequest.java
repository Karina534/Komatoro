package org.example.komatoro.dto.request.tomatoSession;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO для продолжения сессии после паузы
 * @param sessionId
 * @param resumeAt
 */
public record ResumeTomatoSessionDTORequest(
        @NotNull
        Long sessionId,
        @NotNull(message = "Resume time is required")
        Instant resumeAt
) {
}
