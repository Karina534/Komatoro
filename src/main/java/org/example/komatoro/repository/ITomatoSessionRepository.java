package org.example.komatoro.repository;

import org.example.komatoro.model.TomatoSession;
import org.example.komatoro.model.TomatoStatus;
import org.example.komatoro.model.TomatoType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ITomatoSessionRepository extends JpaRepository<TomatoSession, Long> {
    List<TomatoSession> findByUserId(Long id);
    List<TomatoSession> findByTaskId(Long id);
    Optional<TomatoSession> findRunningSessionByUserId(Long userId);
    Optional<TomatoSession> findTomatoSessionByUserIdAndStatus(Long userId, TomatoStatus session);

    Optional<TomatoSession> findFirstByUserIdAndStatusInOrderByStartTimeDesc(
            Long userId,
            Collection<TomatoStatus> statuses
    );
}
















