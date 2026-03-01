package org.example.komatoro.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

/**
 * Сущность статистики томатов пользователя
 */

@Getter
@Setter
@ToString(exclude = "user")
@Entity
@Table(name = "user_daily_stats",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "date"}))
public class UserDailyStats extends BaseEntity{

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;

    @Column(name = "date", nullable = false, updatable = false)
    private LocalDate date = LocalDate.now();

    @Min(0)
    @Column(name = "pomodoro_count", nullable = false)
    private Integer pomodoroCount = 0;

    @Min(0)
    @Column(name = "focus_minutes", nullable = false)
    private Integer focusMinutes = 0;

    public UserDailyStats() {
    }

    public UserDailyStats(User user) {
        this.user = user;
    }

    public UserDailyStats(LocalDate date, Integer pomodoroCount, Integer focusMinutes) {
        this.date = date;
        this.pomodoroCount = pomodoroCount;
        this.focusMinutes = focusMinutes;
    }

    public UserDailyStats(Integer pomodoroCount, Integer focusMinutes) {
        this.pomodoroCount = pomodoroCount;
        this.focusMinutes = focusMinutes;
    }
}
