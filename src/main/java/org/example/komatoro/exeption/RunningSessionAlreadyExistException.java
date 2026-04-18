package org.example.komatoro.exeption;

import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Исключение, которое выбрасывается, если пользователь запустить вторую сессию
 */
public class RunningSessionAlreadyExistException extends BusinessException{
    public RunningSessionAlreadyExistException(Long sessionId, Long userId){
        super(
                "RUNNING_SESSION_ALREADY_EXIST",
                String.format("You can't start two sessions. Check if you already have running session, user %s", userId),
                Map.of("runningSessionId", sessionId,
                        "userId", userId),
                HttpStatus.CONFLICT
        );
    }

    public RunningSessionAlreadyExistException(Long userId){
        super(
                "RUNNING_SESSION_ALREADY_EXIST",
                String.format("You can't start two sessions. Check if you already have running session, user %s", userId)
        );
    }

    public RunningSessionAlreadyExistException(){
        super(
                "RUNNING_SESSION_ALREADY_EXIST",
                "You can't start two sessions. Check if you already have running session"
        );
    }
}
