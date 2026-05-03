package ru.silex.tasktracker.dto;

import java.time.Instant;

public record TimeRecordResponse(
        Long id,
        Long employeeId,
        Long taskId,
        Instant startedAt,
        Instant finishedAt,
        String workDescription
) {
}
