package org.example.komatoro.dto.response.userSettings;

/**
 * DTO ответа с информацией о настройках томатов пользователя
 * @param pomodoroMinutes
 * @param longBreakMinutes
 * @param shortBreakMinutes
 * @param longBreakInterval
 */
public record UserSettingsDTOResponse(
        Integer pomodoroMinutes,
        Integer longBreakMinutes,
        Integer shortBreakMinutes,
        Integer longBreakInterval
) {
}
