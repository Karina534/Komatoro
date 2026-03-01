package org.example.komatoro.service;

import org.example.komatoro.dto.response.userSettings.UserDailyStatsDTOResponse;
import org.example.komatoro.model.UserDailyStats;

import java.time.LocalDate;
import java.util.UUID;

public interface IUserDailyStatsService {
    UserDailyStatsDTOResponse createBasicStats(Long userId);
    UserDailyStatsDTOResponse increaseStats(Long userId, LocalDate date, Integer addPomodoroCount, Integer addFocuseMinutes);
    void deleteStats(Long userId, LocalDate date);
    UserDailyStatsDTOResponse getUserDailyStats(Long userId, LocalDate date);

    UserDailyStats getUserDailyStatsOrCreate(Long userId, LocalDate now);
}
