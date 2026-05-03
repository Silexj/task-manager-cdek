package ru.silex.tasktracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTaskRequest(
        @NotBlank(message = "must not be blank")
        @Size(max = 500, message = "must be at most 500 characters")
        String title,
        @Size(max = 5000, message = "must be at most 5000 characters")
        String description
) {
}
