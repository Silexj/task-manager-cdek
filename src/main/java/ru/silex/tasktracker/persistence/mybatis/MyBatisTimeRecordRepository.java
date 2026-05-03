package ru.silex.tasktracker.persistence.mybatis;

import org.springframework.stereotype.Repository;
import ru.silex.tasktracker.persistence.TimeRecordRepository;
import ru.silex.tasktracker.persistence.model.TimeRecordEntity;

import java.time.Instant;
import java.util.List;

@Repository
public class MyBatisTimeRecordRepository implements TimeRecordRepository {

    private final TimeRecordEntityMapper timeRecordEntityMapper;

    public MyBatisTimeRecordRepository(TimeRecordEntityMapper timeRecordEntityMapper) {
        this.timeRecordEntityMapper = timeRecordEntityMapper;
    }

    @Override
    public TimeRecordEntity save(TimeRecordEntity entity) {
        timeRecordEntityMapper.insert(entity);
        return entity;
    }

    @Override
    public List<TimeRecordEntity> findByEmployeeIdAndStartedAtBetween(
            Long employeeId,
            Instant fromInclusive,
            Instant toInclusive) {
        return timeRecordEntityMapper.selectByEmployeeAndStartedAtBetween(employeeId, fromInclusive, toInclusive);
    }
}
