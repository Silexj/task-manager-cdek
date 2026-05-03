package ru.silex.tasktracker.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.silex.tasktracker.dto.CreateTaskRequest;
import ru.silex.tasktracker.dto.TaskResponse;
import ru.silex.tasktracker.dto.UpdateTaskStatusRequest;
import ru.silex.tasktracker.service.TaskService;

import java.net.URI;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody CreateTaskRequest request) {
        TaskResponse body = taskService.create(request);
        URI location = URI.create("/api/tasks/" + body.id());
        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(body);
    }

    @GetMapping("/{id}")
    public TaskResponse getById(@PathVariable Long id) {
        return taskService.getById(id);
    }

    @PatchMapping("/{id}/status")
    public TaskResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateTaskStatusRequest request) {
        return taskService.updateStatus(id, request);
    }
}
