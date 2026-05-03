package ru.silex.tasktracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateTimeRecordRequest(
        @NotNull(message = "must not be null")
        Long employeeId,
        @NotNull(message = "must not be null")
        Instant startedAt,
        @NotNull(message = "must not be null")
        Instant finishedAt,
        @NotBlank(message = "must not be blank")
        @Size(max = 2000, message = "must be at most 2000 characters")
        String workDescription
) {
}
