package org.example.komatoro.exeption;

import org.springframework.http.HttpStatus;

public class TaskIsCompletedException extends BusinessException{
    public TaskIsCompletedException(Long taskId){
        super(
                "TASK_IS_COMPLETED",
                String.format("Task %s is already completed", taskId),
                HttpStatus.BAD_REQUEST
        );
    }
}
