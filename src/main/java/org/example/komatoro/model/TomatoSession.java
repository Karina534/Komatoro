package org.example.komatoro.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

/**
 * Сущность сессии pomodoro
 */

@Getter
@Setter
@ToString(exclude = {"user", "task"})
@Entity
@Table(name = "tomato_sessions",
    indexes = {
        @Index(name = "idx_tomato_session_user_id", columnList = "user_id"),
        @Index(name = "idx_tomato_session_status", columnList = "status")
    })
public class TomatoSession extends BaseEntity{

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Task task;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, updatable = false)
    private TomatoType type = TomatoType.TIMER;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Column(name = "intended_minutes", nullable = false)
    @Min(1)
    @Max(480)
    private Integer intendedMinutes = 25;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TomatoStatus status = TomatoStatus.RUNNING;

    @Min(0)
    @Column(name = "total_active_minutes")
    private Integer totalActiveMinutes = 0;

    // Должен быть больше или равно startTime
    @Column(name = "last_resume_time")
    private Instant lastResumeTime;

    // Для оптимистичных блокировок, если пользователь отправит два действия одновременно, это убережет от lost update
    @Version
    private Long version;

    public TomatoSession() {
    }

    public TomatoSession(User user, Task task, TomatoType type, Integer intendedMinutes) {
        this.user = user;
        this.task = task;
        this.type = type;
        this.intendedMinutes = intendedMinutes;
    }

    public TomatoSession(User user, Integer intendedMinutes) {
        this.user = user;
        this.intendedMinutes = intendedMinutes;
    }

    public TomatoSession(User user) {
        this.user = user;
    }

    @PrePersist
    protected void onCreate(){
        createdAt = Instant.now();
        if (this.startTime == null){
            startTime = Instant.now();
        }
        if (this.lastResumeTime == null && this.status == TomatoStatus.RUNNING){
            lastResumeTime = this.startTime;
        }
        if (task == null && type == null){
            type = TomatoType.TIMER;
        }
        if (this.status == null){
            this.status = TomatoStatus.RUNNING;
        }
    }

    @PreUpdate
    public void endSession(){
        if (endTime != null && endTime.isBefore(this.startTime)){
            throw new IllegalArgumentException("End time must be after start time");
        }

        if (this.lastResumeTime != null && this.lastResumeTime.isBefore(this.startTime)){
            throw new IllegalArgumentException("Last resume time must be after start time");
        }
    }

}