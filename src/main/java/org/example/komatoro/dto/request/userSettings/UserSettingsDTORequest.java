package org.example.komatoro.dto.request.userSettings;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * DTO для обновления настроек томатов пользователя
 * @param pomodoroMinutes
 * @param longBreakMinutes
 * @param shortBreakMinutes
 * @param longBreakInterval
 */
public record UserSettingsDTORequest(
        @Min(value = 10, message = "PomodoroMinutes must be at least {value} minutes")
        @Max(value = 480, message = "PomodoroMinutes must be at most {value} minutes")
        Integer pomodoroMinutes,

        @Min(value = 5, message = "LongBreakMinutes must be at least {value} minutes")
        @Max(value = 240, message = "LongBreakMinutes must be at most {value} minutes")
        Integer longBreakMinutes,

        @Min(value = 5, message = "ShortBreakMinutes must be at least {value} minutes")
        @Max(value = 120, message = "ShortBreakMinutes must be at most {value} minutes")
        Integer shortBreakMinutes,

        @Min(value = 2, message = "LongBreakInterval must be at least {value}")
        @Max(value = 24, message = "LongBreakInterval must be at most {value}")
        Integer longBreakInterval
) {
}
