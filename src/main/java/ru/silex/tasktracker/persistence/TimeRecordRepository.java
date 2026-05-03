package ru.silex.tasktracker.persistence;

import ru.silex.tasktracker.persistence.model.TimeRecordEntity;

import java.time.Instant;
import java.util.List;

public interface TimeRecordRepository {

    TimeRecordEntity save(TimeRecordEntity entity);

    List<TimeRecordEntity> findByEmployeeIdAndStartedAtBetween(Long employeeId, Instant fromInclusive, Instant toInclusive);
}
