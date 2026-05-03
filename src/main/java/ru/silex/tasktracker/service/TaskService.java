package ru.silex.tasktracker.service;

import ru.silex.tasktracker.dto.CreateTaskRequest;
import ru.silex.tasktracker.dto.TaskResponse;
import ru.silex.tasktracker.dto.UpdateTaskStatusRequest;

public interface TaskService {

    TaskResponse create(CreateTaskRequest request);

    TaskResponse getById(Long id);

    TaskResponse updateStatus(Long id, UpdateTaskStatusRequest request);
}
