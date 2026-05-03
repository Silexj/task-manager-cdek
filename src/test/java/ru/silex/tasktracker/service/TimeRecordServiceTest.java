package ru.silex.tasktracker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.silex.tasktracker.domain.TaskStatus;
import ru.silex.tasktracker.dto.CreateTimeRecordRequest;
import ru.silex.tasktracker.dto.TimeRecordResponse;
import ru.silex.tasktracker.exception.TaskNotFoundException;
import ru.silex.tasktracker.persistence.TaskRepository;
import ru.silex.tasktracker.persistence.TimeRecordRepository;
import ru.silex.tasktracker.persistence.model.TaskEntity;
import ru.silex.tasktracker.persistence.model.TimeRecordEntity;
import ru.silex.tasktracker.service.impl.TimeRecordServiceImpl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Юнит-тесты {@link TimeRecordServiceImpl} против моков репозиториев.
 */
@ExtendWith(MockitoExtension.class)
class TimeRecordServiceTest {

    private static final Instant T0 = Instant.parse("2026-05-01T08:00:00Z");
    private static final Instant T1 = Instant.parse("2026-05-02T08:00:00Z");
    private static final Instant T2 = Instant.parse("2026-05-03T08:00:00Z");

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TimeRecordRepository timeRecordRepository;

    @InjectMocks
    private TimeRecordServiceImpl timeRecordService;

    private TaskEntity task;

    @BeforeEach
    void setUp() {
        task = new TaskEntity(1L, "Work", null, TaskStatus.IN_PROGRESS);
    }

    @Test
    void given_existingTaskAndValidInterval_when_create_then_saves_and_returnsMappedResponse() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(timeRecordRepository.save(any(TimeRecordEntity.class))).thenAnswer(invocation -> {
            TimeRecordEntity e = invocation.getArgument(0);
            e.setId(55L);
            return e;
        });

        CreateTimeRecordRequest req = new CreateTimeRecordRequest(9L, T0, T1, "Coding");
        TimeRecordResponse result = timeRecordService.create(1L, req);

        ArgumentCaptor<TimeRecordEntity> captor = ArgumentCaptor.forClass(TimeRecordEntity.class);
        verify(timeRecordRepository).save(captor.capture());
        TimeRecordEntity saved = captor.getValue();
        assertThat(saved.getEmployeeId()).isEqualTo(9L);
        assertThat(saved.getTaskId()).isEqualTo(1L);
        assertThat(saved.getStartedAt()).isEqualTo(T0);
        assertThat(saved.getFinishedAt()).isEqualTo(T1);
        assertThat(saved.getWorkDescription()).isEqualTo("Coding");

        assertThat(result.id()).isEqualTo(55L);
        assertThat(result.taskId()).isEqualTo(1L);
        assertThat(result.employeeId()).isEqualTo(9L);
        assertThat(result.workDescription()).isEqualTo("Coding");
    }

    @Test
    void given_unknownTask_when_create_then_throwsTaskNotFoundException() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        CreateTimeRecordRequest req = new CreateTimeRecordRequest(1L, T0, T1, "x");

        assertThatThrownBy(() -> timeRecordService.create(999L, req))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void given_finishNotAfterStart_when_create_then_throwsIllegalArgumentException() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        CreateTimeRecordRequest req = new CreateTimeRecordRequest(1L, T1, T0, "bad window");

        assertThatThrownBy(() -> timeRecordService.create(1L, req))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void given_validWindow_when_findByEmployee_then_returnsMappedRows() {
        TimeRecordEntity row = new TimeRecordEntity();
        row.setId(10L);
        row.setEmployeeId(2L);
        row.setTaskId(3L);
        row.setStartedAt(T0);
        row.setFinishedAt(T1);
        row.setWorkDescription("A");

        when(timeRecordRepository.findByEmployeeIdAndStartedAtBetween(eq(2L), eq(T0), eq(T2)))
                .thenReturn(List.of(row));

        List<TimeRecordResponse> result = timeRecordService.findByEmployeeAndPeriod(2L, T0, T2);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(10L);
        assertThat(result.getFirst().employeeId()).isEqualTo(2L);
        assertThat(result.getFirst().taskId()).isEqualTo(3L);
        assertThat(result.getFirst().workDescription()).isEqualTo("A");
    }

    @Test
    void given_invertedPeriod_when_findByEmployee_then_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> timeRecordService.findByEmployeeAndPeriod(2L, T2, T0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
