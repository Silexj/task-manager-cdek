package ru.silex.tasktracker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.silex.tasktracker.domain.TaskStatus;
import ru.silex.tasktracker.dto.CreateTaskRequest;
import ru.silex.tasktracker.dto.TaskResponse;
import ru.silex.tasktracker.dto.UpdateTaskStatusRequest;
import ru.silex.tasktracker.exception.TaskNotFoundException;
import ru.silex.tasktracker.persistence.TaskRepository;
import ru.silex.tasktracker.persistence.model.TaskEntity;
import ru.silex.tasktracker.service.impl.TaskServiceImpl;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Юнит-тесты {@link TaskServiceImpl} против моков {@link TaskRepository}.
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private TaskEntity persistedTask;

    @BeforeEach
    void setUp() {
        persistedTask = new TaskEntity();
        persistedTask.setId(42L);
        persistedTask.setTitle("Draft");
        persistedTask.setDescription("Details");
        persistedTask.setStatus(TaskStatus.IN_PROGRESS);
    }

    @Test
    void given_request_when_create_then_savesEntityWithNewStatus_and_returnsMappedResponse() {
        when(taskRepository.save(any(TaskEntity.class))).thenAnswer(invocation -> {
            TaskEntity e = invocation.getArgument(0);
            e.setId(100L);
            return e;
        });

        CreateTaskRequest req = new CreateTaskRequest("Draft", "Details");
        TaskResponse result = taskService.create(req);

        ArgumentCaptor<TaskEntity> captor = ArgumentCaptor.forClass(TaskEntity.class);
        verify(taskRepository).save(captor.capture());
        TaskEntity saved = captor.getValue();
        assertThat(saved.getTitle()).isEqualTo("Draft");
        assertThat(saved.getDescription()).isEqualTo("Details");
        assertThat(saved.getStatus()).isEqualTo(TaskStatus.NEW);

        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.title()).isEqualTo("Draft");
        assertThat(result.description()).isEqualTo("Details");
        assertThat(result.status()).isEqualTo(TaskStatus.NEW);
    }

    @Test
    void given_existingId_when_getById_then_returnsMappedResponse() {
        when(taskRepository.findById(42L)).thenReturn(Optional.of(persistedTask));

        TaskResponse result = taskService.getById(42L);

        assertThat(result.id()).isEqualTo(42L);
        assertThat(result.title()).isEqualTo("Draft");
        assertThat(result.description()).isEqualTo("Details");
        assertThat(result.status()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    void given_unknownId_when_getById_then_throwsTaskNotFoundException() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getById(99L))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void given_existingTask_when_updateStatus_then_persistsNewStatus_and_returnsResponse() {
        when(taskRepository.findById(42L)).thenReturn(Optional.of(persistedTask));
        when(taskRepository.save(any(TaskEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse result = taskService.updateStatus(42L, new UpdateTaskStatusRequest(TaskStatus.DONE));

        verify(taskRepository).save(persistedTask);
        assertThat(persistedTask.getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(result.status()).isEqualTo(TaskStatus.DONE);
        assertThat(result.id()).isEqualTo(42L);
    }

    @Test
    void given_missingTask_when_updateStatus_then_throwsTaskNotFoundException() {
        when(taskRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateStatus(7L, new UpdateTaskStatusRequest(TaskStatus.DONE)))
                .isInstanceOf(TaskNotFoundException.class);
    }
}
