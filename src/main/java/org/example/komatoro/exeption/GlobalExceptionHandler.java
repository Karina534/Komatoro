package org.example.komatoro.exeption;

import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.*;

/**
 * Глобальный обработчик исключений REST API
 *
 * Этот класс перехватывает исключения, возникающие в контроллерах, и преобразует их в стандартизированные
 * HTTP-ответы с соответствующими кодами состояния и телами ошибок
 *
 * Основные функции:
 * Обработка бизнес-исключений ({@link BusinessException})
 * Валидация входных данных ({@link MethodArgumentNotValidException})
 * Обработка HTTP 404 ошибок ({@link NoHandlerFoundException})
 * Обработка ошибок безопасности ({@link AccessDeniedException})
 * Обработка непредвиденных исключений
 */

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private final Environment environment;

    public GlobalExceptionHandler(Environment environment) {
        this.environment = environment;
    }

    // Обработка кастомных бизнес исключений
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request){
        log.warn("Business exception: code={}, path={}", ex.getCode(), request.getRequestURI(), ex);

        ErrorResponse errorResponse = ex.toErrorResponse(request);

        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }

    // Обработка валидации Spring Validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleArgumentValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request){
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("timestamp", Instant.now());
        response.put("path", request.getRequestURI());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation failed");

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        response.put("errors", errors);

        return ResponseEntity.badRequest().body(response);
    }

    // 404 - маршрут не найден
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoResourceFoundException ex, HttpServletRequest request){
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("ENDPOINT_NOT_FOUND")
                .message("Path not found")
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    // Ошибки path variable конвертации
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ){
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("INVALID_PARAMETER")
                .message(String.format("Parameter '%s' has invalid value: %s",
                        ex.getName(), ex.getValue()))
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse);
    }

    // Ошибки Spring Security
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request){
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("ACCESS_DENIED")
                .message("You do not have enough access")
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    // Обработка всех непредвиденных исключений
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception ex,
            HttpServletRequest request){

        String activeProfile = environment.getProperty("spring.profiles.active", "default");
        boolean isDev = "dev".equals(activeProfile);
        log.error("Unexpected exception at path {}: ", request.getRequestURI(), ex);

        return ResponseEntity.internalServerError().body(ErrorResponse.builder()
                        .code("INTERNAL_SERVER_ERROR")
                        .message(isDev ? ex.getMessage(): "Unexpected server error")
                .build());
    }

    // Обработка ошибок оптимистичной блокировки (для двух одновременных запросов к сессии томатов)
    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockException(
            OptimisticLockException ex,
            HttpServletRequest request){

        String activeProfile = environment.getProperty("spring.profiles.active", "default");
        boolean isDev = "dev".equals(activeProfile);
        log.warn("Optimistic lock exception by path: {}", request.getRequestURI(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("CONCURRENT_MODIFICATION")
                .message(isDev ? ex.getMessage() : "The resource change failed, please repeat later.")
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
}
