package org.example.komatoro.exeption;

import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

/**
 * Исключение, если сессия не была найдена по id
 */
public class SessionNotFoundException extends BusinessException{
    public SessionNotFoundException(Long sessionId){
        super(
                "SESSION_NOT_FOUND",
                String.format("Session with ID %s not found", sessionId),
                Map.of("sessionId", sessionId),
                HttpStatus.NOT_FOUND
        );
    }
}
