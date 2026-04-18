package org.example.komatoro.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

/**
 * Сущность задачи
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "tasks")
public class Task extends BaseEntity{

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", nullable = false)
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 200, message = "Title must be between {min} and {max} characters")
    private String title;

    @Column(name = "description")
    @Size(max = 2000, message = "Description must be at most {max} characters")
    private String description;

    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    public Task() {
    }

    public Task(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public Task(boolean isActive) {
        this.isActive = isActive;
    }

    @PrePersist
    protected void onCreate(){
        if (this.createdAt == null){
            this.createdAt = Instant.now();
        }
    }
}
