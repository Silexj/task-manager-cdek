package ru.silex.tasktracker.service;

import ru.silex.tasktracker.dto.CreateTimeRecordRequest;
import ru.silex.tasktracker.dto.TimeRecordResponse;

import java.time.Instant;
import java.util.List;

public interface TimeRecordService {

    TimeRecordResponse create(Long taskId, CreateTimeRecordRequest request);

    List<TimeRecordResponse> findByEmployeeAndPeriod(Long employeeId, Instant from, Instant to);
}
