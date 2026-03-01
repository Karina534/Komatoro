package org.example.komatoro.exeption;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Абстрактный класс для всех бизнес исключений
 */
@Getter
public abstract class BusinessException extends RuntimeException{
    private final String code;
    private final Instant timestamp;
    private final Map<String, Object> details;
    private final HttpStatus httpStatus;

    public BusinessException(
            String code,
            String message,
            Map<String, Object> details,
            HttpStatus httpStatus) {
        super(message);
        this.code = code;
        this.timestamp = Instant.now();
        this.details = details != null ? details: Map.of();
        this.httpStatus = httpStatus != null ? httpStatus: HttpStatus.INTERNAL_SERVER_ERROR;
    }

    protected BusinessException(String code, String message) {
        this(code, message, Map.of(), HttpStatus.BAD_REQUEST);
    }

    protected BusinessException(String code, String message, HttpStatus httpStatus){
        this(code, message, Map.of(), httpStatus);
    }

    /**
     * Преобразование исключения в ErrorResponse для отправки клиенту
     */
    public ErrorResponse toErrorResponse(HttpServletRequest request){
        return ErrorResponse.builder()
                .errorId(generateErrorId())
                .timestamp(this.timestamp)
                .code(this.code)
                .message(this.getMessage())
                .path(request.getRequestURI())
                .method(request.getMethod())
                .details(this.details)
                .build();
    }

    private String generateErrorId(){
        return "errId-" + UUID.randomUUID();
    }
}
