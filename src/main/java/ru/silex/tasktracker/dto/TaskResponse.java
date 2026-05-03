package ru.silex.tasktracker.dto;

import ru.silex.tasktracker.domain.TaskStatus;

public record TaskResponse(Long id, String title, String description, TaskStatus status) {
}
