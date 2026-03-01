package org.example.komatoro.dto.request.tomatoSession;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO для увелечения времени томата
 * @param sessionId
 * @param addMinutes
 */
public record ExtendTomatoSessionDTORequest(
        Long sessionId,
        @Min(value = 1)
        Integer addMinutes
) {
}
