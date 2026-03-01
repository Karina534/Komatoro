package org.example.komatoro.dto.TemporaryEntityDTO;

import java.time.LocalDate;
import java.util.UUID;

public record UserDailyStatsDTO(
        UUID id,
        UUID userId,
        LocalDate date,
        Integer pomodoroCount,
        Integer focusMinutes
) {

}
