package ru.silex.tasktracker.service.impl;

import org.springframework.stereotype.Service;
import ru.silex.tasktracker.dto.CreateTaskRequest;
import ru.silex.tasktracker.dto.TaskResponse;
import ru.silex.tasktracker.dto.UpdateTaskStatusRequest;
import ru.silex.tasktracker.persistence.TaskRepository;
import ru.silex.tasktracker.service.TaskService;

@Service
public class TaskServiceImpl implements TaskService {

    @SuppressWarnings("unused")
    private final TaskRepository taskRepository;

    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public TaskResponse create(CreateTaskRequest request) {
        throw new UnsupportedOperationException("TaskService#create not implemented yet");
    }

    @Override
    public TaskResponse getById(Long id) {
        throw new UnsupportedOperationException("TaskService#getById not implemented yet");
    }

    @Override
    public TaskResponse updateStatus(Long id, UpdateTaskStatusRequest request) {
        throw new UnsupportedOperationException("TaskService#updateStatus not implemented yet");
    }
}
