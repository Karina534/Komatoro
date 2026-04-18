package org.example.komatoro.dto.request.tomatoSession;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * DTO для остановки сессии
 * @param sessionId
 * @param pausedTime
 */
public record PauseTomatoSessionDTORequest(
        Long sessionId,
        @NotNull(message = "Paused time is required")
        Instant pausedTime
) {
}
