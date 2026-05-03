package ru.silex.tasktracker.service.impl;

import org.springframework.stereotype.Service;
import ru.silex.tasktracker.dto.CreateTaskRequest;
import ru.silex.tasktracker.dto.TaskResponse;
import ru.silex.tasktracker.dto.UpdateTaskStatusRequest;
import ru.silex.tasktracker.service.TaskService;

@Service
public class StubTaskService implements TaskService {

    @Override
    public TaskResponse create(CreateTaskRequest request) {
        throw new UnsupportedOperationException("Task creation is not implemented yet");
    }

    @Override
    public TaskResponse getById(Long id) {
        throw new UnsupportedOperationException("Task lookup is not implemented yet");
    }

    @Override
    public TaskResponse updateStatus(Long id, UpdateTaskStatusRequest request) {
        throw new UnsupportedOperationException("Task status update is not implemented yet");
    }
}
