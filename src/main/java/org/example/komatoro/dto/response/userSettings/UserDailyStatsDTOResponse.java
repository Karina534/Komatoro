package org.example.komatoro.dto.response.userSettings;

import java.time.LocalDate;

/**
 * DTO ответа для статистика пользователя по сессиям
 * @param date
 * @param pomodoroCount
 * @param focusMinutes
 */
public record UserDailyStatsDTOResponse(
        LocalDate date,
        Integer pomodoroCount,
        Integer focusMinutes
) {
}
