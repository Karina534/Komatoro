package org.example.komatoro.exeption;

import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

/**
 * Исключение, которое выбрасывается, если задача не была найдена по id
 */
public class TaskNotFoundException extends BusinessException{

    public TaskNotFoundException(Long taskId){
        super(
                "TASK_NOT_FOUND",
                String.format("Task with ID %s not found", taskId),
                Map.of("taskId", taskId),
                HttpStatus.NOT_FOUND
        );
    }
}
