package org.example.komatoro.dto.response.user;

import org.example.komatoro.model.Role;

import java.time.Instant;

public record UserWithSettingsDtoResponse(
        Long id,
        String username,
        String email,
        Role role,
        Instant createdAt,
        boolean verified,
        Integer pomodoroMinutes,
        Integer longBreakMinutes,
        Integer shortBreakMinutes,
        Integer longBreakInterval
) {
}
