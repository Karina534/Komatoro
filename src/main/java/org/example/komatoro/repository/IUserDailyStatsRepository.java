package org.example.komatoro.repository;

import org.example.komatoro.dto.TemporaryEntityDTO.UserDailyStatsDTO;
import org.example.komatoro.model.UserDailyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IUserDailyStatsRepository extends JpaRepository<UserDailyStats, Long> {
    Optional<UserDailyStats> findByUserIdAndDate(Long userId, LocalDate date);
    List<UserDailyStats> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
}
