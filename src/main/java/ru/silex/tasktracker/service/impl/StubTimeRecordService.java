package ru.silex.tasktracker.service.impl;

import org.springframework.stereotype.Service;
import ru.silex.tasktracker.dto.CreateTimeRecordRequest;
import ru.silex.tasktracker.dto.TimeRecordResponse;
import ru.silex.tasktracker.service.TimeRecordService;

import java.time.Instant;
import java.util.List;

@Service
public class StubTimeRecordService implements TimeRecordService {

    @Override
    public TimeRecordResponse create(Long taskId, CreateTimeRecordRequest request) {
        throw new UnsupportedOperationException("Time record creation is not implemented yet");
    }

    @Override
    public List<TimeRecordResponse> findByEmployeeAndPeriod(Long employeeId, Instant from, Instant to) {
        throw new UnsupportedOperationException("Time record listing is not implemented yet");
    }
}
