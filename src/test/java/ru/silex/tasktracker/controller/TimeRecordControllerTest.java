package ru.silex.tasktracker.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.silex.tasktracker.dto.TimeRecordResponse;
import ru.silex.tasktracker.exception.TaskNotFoundException;
import ru.silex.tasktracker.service.TimeRecordService;
import ru.silex.tasktracker.web.RestApiExceptionHandler;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TimeRecordController.class)
@Import(RestApiExceptionHandler.class)
class TimeRecordControllerTest {

    private static final Instant T0 = Instant.parse("2026-05-01T08:00:00Z");
    private static final Instant T1 = Instant.parse("2026-05-02T08:00:00Z");
    private static final Instant T2 = Instant.parse("2026-05-03T08:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TimeRecordService timeRecordService;

    @Test
    void given_validBody_when_createTimeRecord_then_status201_and_location() throws Exception {
        var body = new TimeRecordResponse(100L, 5L, 1L, T0, T1, "Feature work");
        when(timeRecordService.create(eq(1L), any())).thenReturn(body);

        mockMvc.perform(post("/api/tasks/1/time-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employeeId": 5,
                                  "startedAt": "2026-05-01T08:00:00Z",
                                  "finishedAt": "2026-05-02T08:00:00Z",
                                  "workDescription": "Feature work"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/api/tasks/1/time-records/100"))
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.employeeId").value(5))
                .andExpect(jsonPath("$.taskId").value(1))
                .andExpect(jsonPath("$.workDescription").value("Feature work"));
    }

    @Test
    void given_unknownTaskId_when_createTimeRecord_then_status404() throws Exception {
        when(timeRecordService.create(eq(9L), any()))
                .thenThrow(new TaskNotFoundException(9L));

        mockMvc.perform(post("/api/tasks/9/time-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employeeId": 1,
                                  "startedAt": "2026-05-01T08:00:00Z",
                                  "finishedAt": "2026-05-01T10:00:00Z",
                                  "workDescription": "x"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found: id=9"));
    }

    @Test
    void given_periodInvalidByService_when_createTimeRecord_then_status400() throws Exception {
        doThrow(new IllegalArgumentException("finishedAt must be after startedAt"))
                .when(timeRecordService).create(eq(1L), any());

        mockMvc.perform(post("/api/tasks/1/time-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employeeId": 1,
                                  "startedAt": "2026-05-02T08:00:00Z",
                                  "finishedAt": "2026-05-01T08:00:00Z",
                                  "workDescription": "inverted"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("finishedAt must be after startedAt"));
    }

    @Test
    void given_missingEmployeeId_when_createTimeRecord_then_status400() throws Exception {
        mockMvc.perform(post("/api/tasks/1/time-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "startedAt": "2026-05-01T08:00:00Z",
                                  "finishedAt": "2026-05-01T10:00:00Z",
                                  "workDescription": "x"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("employeeId"));

        verify(timeRecordService, never()).create(any(), any());
    }

    @Test
    void given_blankWorkDescription_when_createTimeRecord_then_status400() throws Exception {
        mockMvc.perform(post("/api/tasks/1/time-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employeeId": 1,
                                  "startedAt": "2026-05-01T08:00:00Z",
                                  "finishedAt": "2026-05-01T10:00:00Z",
                                  "workDescription": "   "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("workDescription"));
    }

    @Test
    void given_workDescriptionTooLong_when_createTimeRecord_then_status400() throws Exception {
        String longText = "z".repeat(2001);
        mockMvc.perform(post("/api/tasks/1/time-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                  "employeeId": 1,
                                  "startedAt": "2026-05-01T08:00:00Z",
                                  "finishedAt": "2026-05-01T10:00:00Z",
                                  "workDescription": "%s"
                                }
                                """, longText)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("workDescription"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("must be at most 2000 characters"));
    }

    @Test
    void given_malformedJson_when_createTimeRecord_then_status400() throws Exception {
        mockMvc.perform(post("/api/tasks/1/time-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not json at all"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed JSON or incompatible types"));
    }

    @Test
    void given_nonNumericTaskId_when_createTimeRecord_then_status400() throws Exception {
        mockMvc.perform(post("/api/tasks/bad-id/time-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employeeId": 1,
                                  "startedAt": "2026-05-01T08:00:00Z",
                                  "finishedAt": "2026-05-01T10:00:00Z",
                                  "workDescription": "x"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for path or query parameter 'taskId'"));
    }

    @Test
    void given_validRange_when_listEmployeeTimeRecords_then_status200_andArray() throws Exception {
        var row = new TimeRecordResponse(1L, 2L, 3L, T0, T1, "A");
        when(timeRecordService.findByEmployeeAndPeriod(2L, T0, T2)).thenReturn(List.of(row));

        mockMvc.perform(get("/api/employees/2/time-records")
                        .param("from", "2026-05-01T08:00:00Z")
                        .param("to", "2026-05-03T08:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].employeeId").value(2))
                .andExpect(jsonPath("$[0].taskId").value(3));
    }

    @Test
    void given_noRecords_when_listEmployeeTimeRecords_then_status200_andEmptyArray() throws Exception {
        when(timeRecordService.findByEmployeeAndPeriod(2L, T0, T2)).thenReturn(List.of());

        mockMvc.perform(get("/api/employees/2/time-records")
                        .param("from", "2026-05-01T08:00:00Z")
                        .param("to", "2026-05-03T08:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void given_missingFrom_when_listEmployeeTimeRecords_then_status400() throws Exception {
        mockMvc.perform(get("/api/employees/2/time-records")
                        .param("to", "2026-05-03T08:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Required request parameter 'from' is missing"));
    }

    @Test
    void given_missingTo_when_listEmployeeTimeRecords_then_status400() throws Exception {
        mockMvc.perform(get("/api/employees/2/time-records")
                        .param("from", "2026-05-01T08:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Required request parameter 'to' is missing"));
    }

    @Test
    void given_invalidInstantQuery_when_listEmployeeTimeRecords_then_status400() throws Exception {
        mockMvc.perform(get("/api/employees/2/time-records")
                        .param("from", "not-a-timestamp")
                        .param("to", "2026-05-03T08:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for path or query parameter 'from'"));
    }

    @Test
    void given_serviceRejectsInvertedPeriod_when_listEmployeeTimeRecords_then_status400() throws Exception {
        when(timeRecordService.findByEmployeeAndPeriod(eq(2L), eq(T2), eq(T0)))
                .thenThrow(new IllegalArgumentException("Invalid period: 'from' must be before or equal to 'to'"));

        mockMvc.perform(get("/api/employees/2/time-records")
                        .param("from", "2026-05-03T08:00:00Z")
                        .param("to", "2026-05-01T08:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid period: 'from' must be before or equal to 'to'"));
    }

    @Test
    void given_nonNumericEmployeeId_when_listEmployeeTimeRecords_then_status400() throws Exception {
        mockMvc.perform(get("/api/employees/x/time-records")
                        .param("from", "2026-05-01T08:00:00Z")
                        .param("to", "2026-05-03T08:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for path or query parameter 'employeeId'"));
    }
}
