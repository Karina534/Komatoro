package org.example.komatoro.dto.request.tomatoSession;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;


/**
 * DTO для начала томата
 * @param taskId
 * @param intendedMinutes
 */
public record StartTomatoSessionDTORequest(
        Long taskId,

        // Может быть null, тогда берем из настроек пользователя
        @Min(value = 2)
        @Max(value = 480)
        Integer intendedMinutes
) {
}
