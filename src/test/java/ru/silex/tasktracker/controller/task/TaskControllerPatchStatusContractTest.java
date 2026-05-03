package ru.silex.tasktracker.controller.task;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.silex.tasktracker.controller.TaskController;
import ru.silex.tasktracker.domain.TaskStatus;
import ru.silex.tasktracker.dto.TaskResponse;
import ru.silex.tasktracker.dto.UpdateTaskStatusRequest;
import ru.silex.tasktracker.exception.TaskNotFoundException;
import ru.silex.tasktracker.service.TaskService;
import ru.silex.tasktracker.web.RestApiExceptionHandler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract: {@code PATCH /api/tasks/{id}/status} — смена статуса и ошибки ввода.
 */
@DisplayName("Task API — смена статуса (PATCH /api/tasks/{id}/status)")
@WebMvcTest(controllers = TaskController.class)
@Import(RestApiExceptionHandler.class)
class TaskControllerPatchStatusContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Test
    void given_validBody_when_patch_then_200() throws Exception {
        var updated = new TaskResponse(5L, "T", null, TaskStatus.DONE);
        when(taskService.updateStatus(eq(5L), any(UpdateTaskStatusRequest.class))).thenReturn(updated);

        mockMvc.perform(patch("/api/tasks/5/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DONE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void given_unknownTask_when_patch_then_404() throws Exception {
        when(taskService.updateStatus(eq(7L), any(UpdateTaskStatusRequest.class)))
                .thenThrow(new TaskNotFoundException(7L));

        mockMvc.perform(patch("/api/tasks/7/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DONE\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found: id=7"));
    }

    @Test
    void given_invalidStatusLiteral_when_patch_then_400_and_service_not_called() throws Exception {
        mockMvc.perform(patch("/api/tasks/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INVALID_STATUS\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed JSON or incompatible types"));

        verify(taskService, never()).updateStatus(any(), any());
    }

    @Test
    void given_missingStatus_when_patch_then_400() throws Exception {
        mockMvc.perform(patch("/api/tasks/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("status"));

        verify(taskService, never()).updateStatus(any(), any());
    }

    @Test
    void given_nonNumericId_when_patch_then_400() throws Exception {
        mockMvc.perform(patch("/api/tasks/not-id/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DONE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for path or query parameter 'id'"));
    }
}
