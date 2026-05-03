package ru.silex.tasktracker.service.impl;

import org.springframework.stereotype.Service;
import ru.silex.tasktracker.dto.CreateTimeRecordRequest;
import ru.silex.tasktracker.dto.TimeRecordResponse;
import ru.silex.tasktracker.exception.TaskNotFoundException;
import ru.silex.tasktracker.persistence.TaskRepository;
import ru.silex.tasktracker.persistence.TimeRecordRepository;
import ru.silex.tasktracker.persistence.model.TimeRecordEntity;
import ru.silex.tasktracker.service.TimeRecordService;

import java.time.Instant;
import java.util.List;

@Service
public class TimeRecordServiceImpl implements TimeRecordService {

    private static final String INVALID_PERIOD_MESSAGE =
            "Invalid period: 'from' must be before or equal to 'to'";
    private static final String INVALID_INTERVAL_MESSAGE = "finishedAt must be after startedAt";

    private final TaskRepository taskRepository;
    private final TimeRecordRepository timeRecordRepository;

    public TimeRecordServiceImpl(TaskRepository taskRepository, TimeRecordRepository timeRecordRepository) {
        this.taskRepository = taskRepository;
        this.timeRecordRepository = timeRecordRepository;
    }

    @Override
    public TimeRecordResponse create(Long taskId, CreateTimeRecordRequest request) {
        requireTaskExists(taskId);
        requireValidInterval(request.startedAt(), request.finishedAt());

        TimeRecordEntity entity = new TimeRecordEntity();
        entity.setTaskId(taskId);
        entity.setEmployeeId(request.employeeId());
        entity.setStartedAt(request.startedAt());
        entity.setFinishedAt(request.finishedAt());
        entity.setWorkDescription(request.workDescription());

        TimeRecordEntity saved = timeRecordRepository.save(entity);
        return toResponse(saved);
    }

    @Override
    public List<TimeRecordResponse> findByEmployeeAndPeriod(Long employeeId, Instant from, Instant to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException(INVALID_PERIOD_MESSAGE);
        }
        return timeRecordRepository
                .findByEmployeeIdAndStartedAtBetween(employeeId, from, to)
                .stream()
                .map(TimeRecordServiceImpl::toResponse)
                .toList();
    }

    private void requireTaskExists(Long taskId) {
        if (taskRepository.findById(taskId).isEmpty()) {
            throw new TaskNotFoundException(taskId);
        }
    }

    private static void requireValidInterval(Instant startedAt, Instant finishedAt) {
        if (!finishedAt.isAfter(startedAt)) {
            throw new IllegalArgumentException(INVALID_INTERVAL_MESSAGE);
        }
    }

    private static TimeRecordResponse toResponse(TimeRecordEntity entity) {
        return new TimeRecordResponse(
                entity.getId(),
                entity.getEmployeeId(),
                entity.getTaskId(),
                entity.getStartedAt(),
                entity.getFinishedAt(),
                entity.getWorkDescription());
    }
}
