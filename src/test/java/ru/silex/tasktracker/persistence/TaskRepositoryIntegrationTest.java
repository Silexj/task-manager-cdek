package ru.silex.tasktracker.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.silex.tasktracker.AppApplication;
import ru.silex.tasktracker.domain.TaskStatus;
import ru.silex.tasktracker.persistence.model.TaskEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = AppApplication.class)
@Transactional
class TaskRepositoryIntegrationTest {

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void given_newTask_when_save_then_idAssigned_and_findById_returnsRow() {
        TaskEntity entity = new TaskEntity();
        entity.setTitle("Spec");
        entity.setDescription("Notes");
        entity.setStatus(TaskStatus.NEW);

        taskRepository.save(entity);

        assertThat(entity.getId()).isNotNull();

        Optional<TaskEntity> found = taskRepository.findById(entity.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Spec");
        assertThat(found.get().getDescription()).isEqualTo("Notes");
        assertThat(found.get().getStatus()).isEqualTo(TaskStatus.NEW);
    }

    @Test
    void given_persistedTask_when_saveWithSameId_then_updatesStatus() {
        TaskEntity entity = new TaskEntity();
        entity.setTitle("A");
        entity.setDescription(null);
        entity.setStatus(TaskStatus.NEW);
        taskRepository.save(entity);
        Long id = entity.getId();

        entity.setStatus(TaskStatus.DONE);
        taskRepository.save(entity);

        Optional<TaskEntity> found = taskRepository.findById(id);
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(TaskStatus.DONE);
    }
}
