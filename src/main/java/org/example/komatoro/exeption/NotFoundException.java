package org.example.komatoro.exeption;

import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Map;

/**
 * Исключение, которое выбрасывается, если объект в базе данных не была найдена по id, какой-то строке
 */
public class NotFoundException extends BusinessException{

    public NotFoundException(Long id, Class<?> clazz) {
        super(
                "NOT_FOUND",
                String.format("Object %s with ID %s not found", clazz, id),
                Map.of("id", id,
                        "class", clazz),
                HttpStatus.NOT_FOUND
        );
    }

    public NotFoundException(String detail, Class<?> clazz){
        super(
                "NOT_FOUND_BY_DETAIL",
                String.format("Object %s with detail %s not found", clazz, detail),
                Map.of("detail", detail,
                        "class", clazz),
                HttpStatus.NOT_FOUND
        );
    }

    public NotFoundException(Long id, LocalDate date, Class<?> clazz){
        super(
                "NOT_FOUND",
                String.format("Object %s with detail %s not found", clazz, id),
                Map.of("detail", id,
                        "date", date,
                        "class", clazz),
                HttpStatus.NOT_FOUND
        );
    }
}
