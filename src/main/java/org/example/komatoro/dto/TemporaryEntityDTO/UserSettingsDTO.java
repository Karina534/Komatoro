package org.example.komatoro.dto.TemporaryEntityDTO;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class UserSettingsDTO {
    private final UUID id;
    private final UUID userId;
    private final Integer pomodoroMinutes;
    private final Integer longBreakMinutes;
    private final Integer shortBreakMinutes;
    private final Integer longBreakInterval;

    public UserSettingsDTO(UUID id, UUID userId, Integer pomodoroMinutes, Integer longBreakMinutes, Integer shortBreakMinutes, Integer longBreakInterval) {
        this.id = id;
        this.userId = userId;
        this.pomodoroMinutes = pomodoroMinutes;
        this.longBreakMinutes = longBreakMinutes;
        this.shortBreakMinutes = shortBreakMinutes;
        this.longBreakInterval = longBreakInterval;
    }
}
