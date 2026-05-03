package ru.silex.tasktracker.dto;

import java.util.List;

public record ErrorResponse(String message, List<FieldViolation> fieldErrors) {

    public static ErrorResponse of(String message) {
        return new ErrorResponse(message, List.of());
    }

    public static ErrorResponse validation(String message, List<FieldViolation> fieldErrors) {
        return new ErrorResponse(message, fieldErrors);
    }
}
