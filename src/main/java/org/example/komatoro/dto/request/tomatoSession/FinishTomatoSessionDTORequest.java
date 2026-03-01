package org.example.komatoro.dto.request.tomatoSession;

import jakarta.validation.constraints.NotNull;
import org.example.komatoro.model.TomatoStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO для завершения томата или прерывания
 * @param sessionId
 */
public record FinishTomatoSessionDTORequest(
        Long sessionId,

        @NotNull(message = "Type is required. Can be INTERRUPTED and COMPLETED")
        TomatoStatus status
) {
}
