package org.example.komatoro.repository;

import org.example.komatoro.dto.TemporaryEntityDTO.TaskDTO;
import org.example.komatoro.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ITaskRepository extends JpaRepository<Task, Long> {
    Optional<Task> findByTitle(String title);
    List<Task> findByUserId(Long id);
}
