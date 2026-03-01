package org.example.komatoro.dto.request.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO запроса для создания задачи.
 * @param title
 * @param description
 */
public record CreateTaskDTORequest(
        @NotBlank(message = "Title is required")
        @Size(min = 1, max = 200, message = "Title must be between {min} and {max} characters")
        String title,

        @Size(max = 2000, message = "Description must be at most {max} characters")
        String description
) {
}
