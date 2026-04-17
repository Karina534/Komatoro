package org.example.komatoro.service;

import jakarta.validation.constraints.NotNull;
import org.example.komatoro.dto.response.userSettings.UserDailyStatsDTOResponse;
import org.example.komatoro.exeption.NotFoundException;
import org.example.komatoro.model.UserDailyStats;
import org.example.komatoro.repository.IUserDailyStatsRepository;
import org.example.komatoro.repository.IUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Transactional
@Service
public class UserDailyStatsService implements IUserDailyStatsService{
    private final IUserDailyStatsRepository repository;
    private final IUserRepository userRepository;

    public UserDailyStatsService(IUserDailyStatsRepository repository, IUserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Override
    public UserDailyStatsDTOResponse createBasicStats(Long userId) {
        UserDailyStats stats = new UserDailyStats(userRepository.getReferenceById(userId));

        UserDailyStats savedStats = repository.save(stats);
        return this.convertToResponseDto(savedStats);
    }

    @Override
    public UserDailyStatsDTOResponse increaseStats(
            @NotNull Long userId,
            @NotNull LocalDate date,
            Integer addPomodoroCount,
            Integer addFocusMinutes) {

        UserDailyStats dailyStats = repository.findByUserIdAndDate(userId, date)
                .orElseThrow(() -> new NotFoundException(userId, date, UserDailyStats.class));

        if (addPomodoroCount != null) {
            if (addPomodoroCount < 0){
                throw new IllegalArgumentException("Pomodoro count increment must be positive");
            }
            dailyStats.setPomodoroCount(dailyStats.getPomodoroCount() + addPomodoroCount);
        }

        if (addFocusMinutes != null){
            if (addFocusMinutes < 0){
                throw new IllegalArgumentException("Pomodoro count increment must be positive");
            }
            dailyStats.setFocusMinutes(dailyStats.getFocusMinutes() + addFocusMinutes);
        }

        return this.convertToResponseDto(repository.save(dailyStats));
    }

    @Override
    public void deleteStats(Long userId, LocalDate date) {
        UserDailyStats dailyStats = repository.findByUserIdAndDate(userId, date)
                .orElseThrow(() -> new NotFoundException(userId, date, UserDailyStats.class));

        repository.delete(dailyStats);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDailyStatsDTOResponse getUserDailyStats(Long userId, LocalDate date) {
        UserDailyStats dailyStats = repository.findByUserIdAndDate(userId, date)
                .orElseThrow(() -> new NotFoundException(userId, date, UserDailyStats.class));

        return this.convertToResponseDto(dailyStats);
    }

    public UserDailyStats getUserDailyStatsOrCreate(Long userId, LocalDate date){
        return repository.findByUserIdAndDate(userId, date)
                .orElseGet(() -> {
                    UserDailyStats stats = new UserDailyStats(userRepository.getReferenceById(userId));
                    return repository.save(stats);
                });
    }

    private UserDailyStatsDTOResponse convertToResponseDto(UserDailyStats stats){
        return new UserDailyStatsDTOResponse(
                stats.getDate(),
                stats.getPomodoroCount(),
                stats.getFocusMinutes()
        );
    }
}
