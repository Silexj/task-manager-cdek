package ru.silex.tasktracker.persistence.support;

import org.springframework.stereotype.Repository;
import ru.silex.tasktracker.persistence.TimeRecordRepository;
import ru.silex.tasktracker.persistence.model.TimeRecordEntity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Временная заглушка для подъёма контекста; будет заменена реализацией MyBatis/JDBC.
 */
@Repository
public class SpringTimeRecordRepositoryStub implements TimeRecordRepository {

    private final ConcurrentMap<Long, TimeRecordEntity> store = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    @Override
    public TimeRecordEntity save(TimeRecordEntity entity) {
        if (entity.getId() == null) {
            entity.setId(idSequence.getAndIncrement());
        }
        store.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public List<TimeRecordEntity> findByEmployeeIdAndStartedAtBetween(
            Long employeeId,
            Instant fromInclusive,
            Instant toInclusive) {
        List<TimeRecordEntity> result = new ArrayList<>();
        for (TimeRecordEntity e : store.values()) {
            if (!employeeId.equals(e.getEmployeeId())) {
                continue;
            }
            Instant start = e.getStartedAt();
            if (!start.isBefore(fromInclusive) && !start.isAfter(toInclusive)) {
                result.add(e);
            }
        }
        return result;
    }
}
