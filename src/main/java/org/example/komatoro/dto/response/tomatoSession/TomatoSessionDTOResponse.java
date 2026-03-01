package org.example.komatoro.dto.response.tomatoSession;

import org.example.komatoro.model.TomatoStatus;
import org.example.komatoro.model.TomatoType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO ответа для сессии
 * @param id
 * @param userId
 * @param taskId
 * @param type
 * @param createdAt
 * @param startTime
 * @param endTime
 * @param intendedMinutes
 * @param status
 * @param totalActiveMinutes
 * @param lastResumeTime
 */
public record TomatoSessionDTOResponse(
        Long id,
        Long userId,
        Long taskId,
        TomatoType type,
        Instant createdAt,
        Instant startTime,
        Instant endTime,
        Integer intendedMinutes,
        TomatoStatus status,
        Integer totalActiveMinutes,
        Instant lastResumeTime
) {
}
