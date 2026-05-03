package ru.silex.tasktracker.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.silex.tasktracker.AppApplication;
import ru.silex.tasktracker.domain.TaskStatus;
import ru.silex.tasktracker.persistence.model.TaskEntity;
import ru.silex.tasktracker.persistence.model.TimeRecordEntity;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = AppApplication.class)
@Transactional
class TimeRecordRepositoryIntegrationTest {

    private static final Instant T0 = Instant.parse("2026-05-01T10:00:00Z");
    private static final Instant T1 = Instant.parse("2026-05-01T12:00:00Z");
    private static final Instant T2 = Instant.parse("2026-05-03T08:00:00Z");

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TimeRecordRepository timeRecordRepository;

    @Test
    void given_savedTask_when_saveTimeRecord_then_selectByPeriodReturnsRow() {
        TaskEntity task = new TaskEntity();
        task.setTitle("Work");
        task.setDescription(null);
        task.setStatus(TaskStatus.IN_PROGRESS);
        taskRepository.save(task);

        TimeRecordEntity row = new TimeRecordEntity();
        row.setEmployeeId(7L);
        row.setTaskId(task.getId());
        row.setStartedAt(T0);
        row.setFinishedAt(T1);
        row.setWorkDescription("Implementation");
        timeRecordRepository.save(row);

        assertThat(row.getId()).isNotNull();

        List<TimeRecordEntity> hits = timeRecordRepository.findByEmployeeIdAndStartedAtBetween(7L, T0, T2);
        assertThat(hits).hasSize(1);
        assertThat(hits.getFirst().getTaskId()).isEqualTo(task.getId());
        assertThat(hits.getFirst().getWorkDescription()).isEqualTo("Implementation");
    }

    @Test
    void given_recordOutsideWindow_when_selectByPeriod_then_excluded() {
        TaskEntity task = new TaskEntity();
        task.setTitle("W");
        task.setDescription(null);
        task.setStatus(TaskStatus.NEW);
        taskRepository.save(task);

        TimeRecordEntity row = new TimeRecordEntity();
        row.setEmployeeId(1L);
        row.setTaskId(task.getId());
        row.setStartedAt(Instant.parse("2026-06-01T00:00:00Z"));
        row.setFinishedAt(Instant.parse("2026-06-01T01:00:00Z"));
        row.setWorkDescription("Later");
        timeRecordRepository.save(row);

        List<TimeRecordEntity> hits = timeRecordRepository.findByEmployeeIdAndStartedAtBetween(1L, T0, T2);
        assertThat(hits).isEmpty();
    }
}
