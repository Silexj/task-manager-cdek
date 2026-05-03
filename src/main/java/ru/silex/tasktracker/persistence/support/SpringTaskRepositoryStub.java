package ru.silex.tasktracker.persistence.support;

import org.springframework.stereotype.Repository;
import ru.silex.tasktracker.persistence.TaskRepository;
import ru.silex.tasktracker.persistence.model.TaskEntity;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Временная заглушка для подъёма контекста; будет заменена реализацией MyBatis/JDBC.
 */
@Repository
public class SpringTaskRepositoryStub implements TaskRepository {

    private final ConcurrentMap<Long, TaskEntity> store = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    @Override
    public Optional<TaskEntity> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public TaskEntity save(TaskEntity task) {
        if (task.getId() == null) {
            task.setId(idSequence.getAndIncrement());
        }
        store.put(task.getId(), task);
        return task;
    }
}
