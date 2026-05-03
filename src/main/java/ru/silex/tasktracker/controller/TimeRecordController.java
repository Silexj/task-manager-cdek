package ru.silex.tasktracker.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.silex.tasktracker.dto.CreateTimeRecordRequest;
import ru.silex.tasktracker.dto.TimeRecordResponse;
import ru.silex.tasktracker.service.TimeRecordService;

import java.net.URI;
import java.time.Instant;
import java.util.List;

@RestController
public class TimeRecordController {

    private final TimeRecordService timeRecordService;

    public TimeRecordController(TimeRecordService timeRecordService) {
        this.timeRecordService = timeRecordService;
    }

    @PostMapping("/api/tasks/{taskId}/time-records")
    public ResponseEntity<TimeRecordResponse> create(
            @PathVariable Long taskId,
            @Valid @RequestBody CreateTimeRecordRequest request) {
        TimeRecordResponse body = timeRecordService.create(taskId, request);
        URI location = URI.create("/api/tasks/" + taskId + "/time-records/" + body.id());
        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(body);
    }

    @GetMapping("/api/employees/{employeeId}/time-records")
    public List<TimeRecordResponse> listByEmployeeAndPeriod(
            @PathVariable Long employeeId,
            @RequestParam Instant from,
            @RequestParam Instant to) {
        return timeRecordService.findByEmployeeAndPeriod(employeeId, from, to);
    }
}
