package org.example.komatoro.exeption;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * DTO ошибки, чтобы возвращать на фронте
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Builder
public class ErrorResponse {
    private final String errorId;
    private final Instant timestamp;
    private final String code;
    private final String message;

    private final String path;
    private final String method;

    private final Map<String, Object> details;
}
