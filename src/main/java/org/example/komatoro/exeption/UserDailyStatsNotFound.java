package org.example.komatoro.exeption;

import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public class UserDailyStatsNotFound extends BusinessException{
    public UserDailyStatsNotFound(Long userId, LocalDate date){
        super(
                "USER_DAILY_STATS_NOT_FOUND",
                String.format("User daily stats table with ID %s and date %s not found", userId, date),
                Map.of("userId", userId,
                        "date", date),
                HttpStatus.NOT_FOUND
        );
    }
}
