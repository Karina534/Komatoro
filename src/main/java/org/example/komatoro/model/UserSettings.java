package org.example.komatoro.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Сущность настроек томатов пользователя
 */

@Getter
@Setter
@ToString(exclude = "user")
@Entity
@Table(name = "user_settings")
public class UserSettings extends BaseEntity{

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @NotNull
    private User user;

    @Min(10)
    @Max(480)
    @Column(name = "pomodoro_minutes", nullable = false)
    private Integer pomodoroMinutes = 25;

    @Min(5)
    @Max(60)
    @Column(name = "long_break_minutes", nullable = false)
    private Integer longBreakMinutes = 15;

    @Min(1)
    @Max(60)
    @Column(name = "short_break_minutes", nullable = false)
    private Integer shortBreakMinutes = 5;

    @Min(2)
    @Max(20)
    @Column(name = "long_break_interval", nullable = false)
    private Integer longBreakInterval = 4;

    public UserSettings() {
    }

    public UserSettings(User user) {
        this.user = user;
    }

    public UserSettings(Integer pomodoroMinutes, Integer longBreakMinutes, Integer shortBreakMinutes, Integer longBreakInterval) {
        this.pomodoroMinutes = pomodoroMinutes;
        this.longBreakMinutes = longBreakMinutes;
        this.shortBreakMinutes = shortBreakMinutes;
        this.longBreakInterval = longBreakInterval;
    }

    public UserSettings(User user, Integer pomodoroMinutes, Integer longBreakMinutes, Integer shortBreakMinutes, Integer longBreakInterval) {
        this.user = user;
        this.pomodoroMinutes = pomodoroMinutes;
        this.longBreakMinutes = longBreakMinutes;
        this.shortBreakMinutes = shortBreakMinutes;
        this.longBreakInterval = longBreakInterval;
    }
}
