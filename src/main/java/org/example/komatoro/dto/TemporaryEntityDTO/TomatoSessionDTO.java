package org.example.komatoro.dto.TemporaryEntityDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.komatoro.model.TomatoStatus;
import org.example.komatoro.model.TomatoType;

import java.time.Instant;
import java.time.LocalDateTime;

public record TomatoSessionDTO(
        Long id,
        Long userId,
        Long taskId,
        TomatoType type,
        LocalDateTime createdAt,
        @Schema(description = "Start time in ISO 8601 format", example = "2024-01-01T10:00:00Z")
        Instant startTime,
        @Schema(description = "End time in ISO 8601 format", example = "2024-01-01T10:25:00Z")
        Instant endTime,
        @Schema(description = "Based time of tomato from user settings, 25 by default", example = "25")
        Integer intendedMinutes,
        TomatoStatus status,
        @Schema(description = "Actual time spent on tomato in minutes", example = "23")
        Integer totalActiveMinutes,
        Instant lastResumeTime
) {
    public TomatoSessionDTO(Long id, Instant endTime, TomatoStatus status, Integer totalActiveMinutes, Instant lastResumeTime) {
        this(
                id,
                null,
                null,
                null,
                null,
                null,
                endTime,
                null,
                status,
                totalActiveMinutes,
                lastResumeTime);
    }

    public TomatoSessionDTO(Long id, Integer intendedMinutes) {
        this(
                id,
                null,
                null,
                null,
                null,
                null,
                null,
                intendedMinutes,
                null,
                null,
                null
        );
    }

}
