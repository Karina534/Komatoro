package org.example.komatoro.exeption;

import org.example.komatoro.model.TomatoStatus;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Исключение, если пользователь пытается изменить статус сессии на недопустимый
 */
public class InvalidSessionStatusException extends BusinessException{
    public InvalidSessionStatusException(TomatoStatus status){
        super(
                "INVALID_SESSION_STATUS",
                String.format("You try to change session status on unsupported %s", status)
        );
    }

    public InvalidSessionStatusException(Long sessionId, TomatoStatus status){
        super(
                "INVALID_SESSION_STATUS",
                String.format("You try to change session status on unsupported %s", status),
                Map.of("sessionId", sessionId,
                        "putStatus", status),
                HttpStatus.CONFLICT
        );
    }
}
