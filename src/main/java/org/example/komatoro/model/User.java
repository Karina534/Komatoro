package org.example.komatoro.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Сущность пользователя
 */

@Getter
@ToString(exclude = {"password", "dailyStats", "settings"})
@Entity
@Table(name = "users")
public class User extends BaseEntity{
    @Setter
    @Column(name = "username", nullable = false)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between {min} and {max} characters")
    private String username;

    @Setter
    @Column(name = "email", nullable = false, unique = true)
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Setter
    @JsonIgnore
    @Column(name = "password", nullable = false)
    @Size(min = 6, max = 100, message = "Password must be between {min} and {max} characters")
    private String password;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.USER;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Setter
    @Column(name = "verified", nullable = false)
    private boolean verified = false;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<UserDailyStats> dailyStats = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private UserSettings settings;

    public User() {
    }

    public User(String username, String email, String password, Role role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    @PrePersist
    protected void onCreate(){
        if (this.createdAt == null){
            this.createdAt = Instant.now();
        }

        if (settings == null) {
            settings = this.createDefaultSettings();
        }
    }

    public void addDailyStats(UserDailyStats stats){
        stats.setUser(this);
        this.dailyStats.add(stats);
    }

    public UserDailyStats getTodayStats(){
        LocalDate today = LocalDate.now();
        return dailyStats.stream()
                .filter(stats -> stats.getDate().equals(today))
                .findFirst()
                .orElse(null);
    }

    public UserSettings getSettings() {
        if (settings == null) {
            settings = this.createDefaultSettings();
        }
        return settings;
    }

    public void setSettings(UserSettings settings){
        settings.setUser(this);
        this.settings = settings;
    }

    private UserSettings createDefaultSettings() {
        UserSettings defaultSettings = new UserSettings();
        defaultSettings.setUser(this);
        return defaultSettings;
    }

}

