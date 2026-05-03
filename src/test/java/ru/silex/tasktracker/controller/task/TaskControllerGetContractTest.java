package ru.silex.tasktracker.controller.task;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import ru.silex.tasktracker.controller.TaskController;
import ru.silex.tasktracker.domain.TaskStatus;
import ru.silex.tasktracker.dto.TaskResponse;
import ru.silex.tasktracker.exception.TaskNotFoundException;
import ru.silex.tasktracker.service.TaskService;
import ru.silex.tasktracker.web.RestApiExceptionHandler;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract: {@code GET /api/tasks/{id}} — выдача задачи и ошибки по идентификатору.
 */
@DisplayName("Task API — получение по id (GET /api/tasks/{id})")
@WebMvcTest(controllers = TaskController.class)
@Import(RestApiExceptionHandler.class)
class TaskControllerGetContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Test
    void given_existingId_when_get_then_200() throws Exception {
        var task = new TaskResponse(10L, "T", "D", TaskStatus.IN_PROGRESS);
        when(taskService.getById(10L)).thenReturn(task);

        mockMvc.perform(get("/api/tasks/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void given_unknownId_when_get_then_404() throws Exception {
        when(taskService.getById(99L)).thenThrow(new TaskNotFoundException(99L));

        mockMvc.perform(get("/api/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found: id=99"))
                .andExpect(jsonPath("$.fieldErrors", hasSize(0)));
    }

    @Test
    void given_nonNumericId_when_get_then_400() throws Exception {
        mockMvc.perform(get("/api/tasks/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for path or query parameter 'id'"));
    }
}
