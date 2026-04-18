package org.example.komatoro.dto.response.task;

import java.time.Instant;

/**
 * DTO ответа для задачи
 * @param taskId
 * @param title
 * @param description
 * @param isActive
 * @param createdAt
 */
public record TaskDTOResponse(
        Long taskId,
        String title,
        String description,
        boolean isActive,
        Instant createdAt
) {
}
