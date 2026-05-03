package ru.silex.tasktracker.service.impl;

import org.springframework.stereotype.Service;
import ru.silex.tasktracker.domain.TaskStatus;
import ru.silex.tasktracker.dto.CreateTaskRequest;
import ru.silex.tasktracker.dto.TaskResponse;
import ru.silex.tasktracker.dto.UpdateTaskStatusRequest;
import ru.silex.tasktracker.exception.TaskNotFoundException;
import ru.silex.tasktracker.persistence.TaskRepository;
import ru.silex.tasktracker.persistence.model.TaskEntity;
import ru.silex.tasktracker.service.TaskService;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public TaskResponse create(CreateTaskRequest request) {
        TaskEntity entity = new TaskEntity();
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setStatus(TaskStatus.NEW);
        TaskEntity saved = taskRepository.save(entity);
        return toResponse(saved);
    }

    @Override
    public TaskResponse getById(Long id) {
        TaskEntity entity = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        return toResponse(entity);
    }

    @Override
    public TaskResponse updateStatus(Long id, UpdateTaskStatusRequest request) {
        TaskEntity entity = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        entity.setStatus(request.status());
        TaskEntity saved = taskRepository.save(entity);
        return toResponse(saved);
    }

    private static TaskResponse toResponse(TaskEntity entity) {
        return new TaskResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getStatus());
    }
}
