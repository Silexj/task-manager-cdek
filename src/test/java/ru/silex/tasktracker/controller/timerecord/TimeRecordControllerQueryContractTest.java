package ru.silex.tasktracker.controller.timerecord;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import ru.silex.tasktracker.controller.TimeRecordController;
import ru.silex.tasktracker.dto.TimeRecordResponse;
import ru.silex.tasktracker.service.TimeRecordService;
import ru.silex.tasktracker.web.RestApiExceptionHandler;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.silex.tasktracker.controller.timerecord.TimeRecordContractFixtures.T0;
import static ru.silex.tasktracker.controller.timerecord.TimeRecordContractFixtures.T1;
import static ru.silex.tasktracker.controller.timerecord.TimeRecordContractFixtures.T2;

/**
 * Contract: {@code GET /api/employees/{employeeId}/time-records} с {@code from}, {@code to}.
 */
@DisplayName("Time record API — выборка по сотруднику и периоду")
@WebMvcTest(controllers = TimeRecordController.class)
@Import(RestApiExceptionHandler.class)
class TimeRecordControllerQueryContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TimeRecordService timeRecordService;

    @Test
    void given_validParams_when_get_then_200_array() throws Exception {
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
    void given_noRows_when_get_then_200_emptyArray() throws Exception {
        when(timeRecordService.findByEmployeeAndPeriod(2L, T0, T2)).thenReturn(List.of());

        mockMvc.perform(get("/api/employees/2/time-records")
                        .param("from", "2026-05-01T08:00:00Z")
                        .param("to", "2026-05-03T08:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void given_missingFrom_when_get_then_400() throws Exception {
        mockMvc.perform(get("/api/employees/2/time-records")
                        .param("to", "2026-05-03T08:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Required request parameter 'from' is missing"));
    }

    @Test
    void given_missingTo_when_get_then_400() throws Exception {
        mockMvc.perform(get("/api/employees/2/time-records")
                        .param("from", "2026-05-01T08:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Required request parameter 'to' is missing"));
    }

    @Test
    void given_invalidFromInstant_when_get_then_400() throws Exception {
        mockMvc.perform(get("/api/employees/2/time-records")
                        .param("from", "not-a-timestamp")
                        .param("to", "2026-05-03T08:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for path or query parameter 'from'"));
    }

    @Test
    void given_serviceRejectsInvertedPeriod_when_get_then_400() throws Exception {
        when(timeRecordService.findByEmployeeAndPeriod(eq(2L), eq(T2), eq(T0)))
                .thenThrow(new IllegalArgumentException("Invalid period: 'from' must be before or equal to 'to'"));

        mockMvc.perform(get("/api/employees/2/time-records")
                        .param("from", "2026-05-03T08:00:00Z")
                        .param("to", "2026-05-01T08:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid period: 'from' must be before or equal to 'to'"));
    }

    @Test
    void given_nonNumericEmployeeId_when_get_then_400() throws Exception {
        mockMvc.perform(get("/api/employees/x/time-records")
                        .param("from", "2026-05-01T08:00:00Z")
                        .param("to", "2026-05-03T08:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for path or query parameter 'employeeId'"));
    }
}
