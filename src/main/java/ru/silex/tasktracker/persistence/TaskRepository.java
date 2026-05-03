package ru.silex.tasktracker.persistence;

import ru.silex.tasktracker.persistence.model.TaskEntity;

import java.util.Optional;

public interface TaskRepository {

    Optional<TaskEntity> findById(Long id);

    TaskEntity save(TaskEntity task);
}
