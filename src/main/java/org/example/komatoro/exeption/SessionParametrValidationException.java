package org.example.komatoro.exeption;

import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Исключение, если пользователь пытается установить значение, которое противоречит ограничению на поле сессии
 */
public class SessionParametrValidationException extends BusinessException {
    public SessionParametrValidationException(String param){
        super(
                "INVALID_SESSION_PARAMETER",
                String.format("You try to update entity with unsupported parameter %s ", param),
                Map.of("param", param),
                HttpStatus.UNPROCESSABLE_ENTITY
        );
    }
}
