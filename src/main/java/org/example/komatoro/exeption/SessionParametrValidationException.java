package org.example.komatoro.exeption;

import org.example.komatoro.exeption.BusinessException;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Исключение, если пользователь пытается установить значение, которое противоречит ограничению на поле сессии
 */
public class SessionParametrValidationException extends BusinessException {
    public SessionParametrValidationException(String param){
        super(
                "INVALID_SESSION_PARAMETR",
                String.format("You try to update entity with unsupported parametr %s ", param),
                Map.of("param", param),
                HttpStatus.UNPROCESSABLE_ENTITY
        );
    }
}
