package ru.silex.tasktracker.dto;

import jakarta.validation.constraints.NotNull;
import ru.silex.tasktracker.domain.TaskStatus;

public record UpdateTaskStatusRequest(@NotNull(message = "must not be null") TaskStatus status) {
}
