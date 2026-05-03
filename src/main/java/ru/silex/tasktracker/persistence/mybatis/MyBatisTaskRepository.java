package ru.silex.tasktracker.persistence.mybatis;

import org.springframework.stereotype.Repository;
import ru.silex.tasktracker.persistence.TaskRepository;
import ru.silex.tasktracker.persistence.model.TaskEntity;

import java.util.Optional;

@Repository
public class MyBatisTaskRepository implements TaskRepository {

    private final TaskEntityMapper taskEntityMapper;

    public MyBatisTaskRepository(TaskEntityMapper taskEntityMapper) {
        this.taskEntityMapper = taskEntityMapper;
    }

    @Override
    public Optional<TaskEntity> findById(Long id) {
        return Optional.ofNullable(taskEntityMapper.selectById(id));
    }

    @Override
    public TaskEntity save(TaskEntity task) {
        if (task.getId() == null) {
            taskEntityMapper.insert(task);
        } else {
            taskEntityMapper.update(task);
        }
        return task;
    }
}
