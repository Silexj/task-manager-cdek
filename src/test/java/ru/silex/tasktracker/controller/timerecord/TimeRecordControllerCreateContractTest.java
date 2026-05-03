package ru.silex.tasktracker.controller.timerecord;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.silex.tasktracker.controller.TimeRecordController;
import ru.silex.tasktracker.dto.TimeRecordResponse;
import ru.silex.tasktracker.exception.TaskNotFoundException;
import ru.silex.tasktracker.service.TimeRecordService;
import ru.silex.tasktracker.web.RestApiExceptionHandler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.silex.tasktracker.controller.timerecord.TimeRecordContractFixtures.T0;
import static ru.silex.tasktracker.controller.timerecord.TimeRecordContractFixtures.T1;

/**
 * Contract: {@code POST /api/tasks/{taskId}/time-records} — запись времени и ошибки.
 */
@DisplayName("Time record API — создание (POST /api/tasks/{taskId}/time-records)")
@WebMvcTest(controllers = TimeRecordController.class)
@Import(RestApiExceptionHandler.class)
class TimeRecordControllerCreateContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TimeRecordService timeRecordService;

    @Test
    void given_validBody_when_post_then_201_and_location() throws Exception {
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
    void given_unknownTaskId_when_post_then_404() throws Exception {
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
    void given_serviceRejectsPeriod_when_post_then_400() throws Exception {
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
    void given_missingEmployeeId_when_post_then_400() throws Exception {
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
    void given_blankWorkDescription_when_post_then_400() throws Exception {
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
    void given_workDescriptionTooLong_when_post_then_400() throws Exception {
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
    void given_malformedJson_when_post_then_400() throws Exception {
        mockMvc.perform(post("/api/tasks/1/time-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not json at all"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed JSON or incompatible types"));
    }

    @Test
    void given_nonNumericTaskId_when_post_then_400() throws Exception {
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
}
