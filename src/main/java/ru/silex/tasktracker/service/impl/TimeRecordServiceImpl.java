package ru.silex.tasktracker.service.impl;

import org.springframework.stereotype.Service;
import ru.silex.tasktracker.dto.CreateTimeRecordRequest;
import ru.silex.tasktracker.dto.TimeRecordResponse;
import ru.silex.tasktracker.persistence.TaskRepository;
import ru.silex.tasktracker.persistence.TimeRecordRepository;
import ru.silex.tasktracker.service.TimeRecordService;

import java.time.Instant;
import java.util.List;

@Service
public class TimeRecordServiceImpl implements TimeRecordService {

    @SuppressWarnings("unused")
    private final TaskRepository taskRepository;
    @SuppressWarnings("unused")
    private final TimeRecordRepository timeRecordRepository;

    public TimeRecordServiceImpl(TaskRepository taskRepository, TimeRecordRepository timeRecordRepository) {
        this.taskRepository = taskRepository;
        this.timeRecordRepository = timeRecordRepository;
    }

    @Override
    public TimeRecordResponse create(Long taskId, CreateTimeRecordRequest request) {
        throw new UnsupportedOperationException("TimeRecordService#create not implemented yet");
    }

    @Override
    public List<TimeRecordResponse> findByEmployeeAndPeriod(Long employeeId, Instant from, Instant to) {
        throw new UnsupportedOperationException("TimeRecordService#findByEmployeeAndPeriod not implemented yet");
    }
}
