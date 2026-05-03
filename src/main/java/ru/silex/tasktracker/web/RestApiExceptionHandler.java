package ru.silex.tasktracker.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.core.convert.ConversionFailedException;
import ru.silex.tasktracker.dto.ErrorResponse;
import ru.silex.tasktracker.dto.FieldViolation;
import ru.silex.tasktracker.exception.TaskNotFoundException;

import java.util.List;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestApiExceptionHandler {

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTaskNotFound(TaskNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.of(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<FieldViolation> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new FieldViolation(fe.getField(), fe.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest().body(ErrorResponse.validation("Validation failed", violations));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(ErrorResponse.of("Malformed JSON or incompatible types"));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        String msg = "Required request parameter '%s' is missing".formatted(ex.getParameterName());
        return ResponseEntity.badRequest().body(ErrorResponse.of(msg));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String name = ex.getName();
        String msg = "Invalid value for path or query parameter '%s'".formatted(name);
        return ResponseEntity.badRequest().body(ErrorResponse.of(msg));
    }

    @ExceptionHandler(ConversionFailedException.class)
    public ResponseEntity<ErrorResponse> handleConversionFailed(ConversionFailedException ex) {
        return ResponseEntity.badRequest().body(ErrorResponse.of("Invalid parameter format"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ErrorResponse.of(ex.getMessage()));
    }
}
