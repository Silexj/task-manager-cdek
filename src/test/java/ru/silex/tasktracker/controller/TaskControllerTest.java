package ru.silex.tasktracker.controller;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.silex.tasktracker.domain.TaskStatus;
import ru.silex.tasktracker.dto.CreateTaskRequest;
import ru.silex.tasktracker.dto.TaskResponse;
import ru.silex.tasktracker.dto.UpdateTaskStatusRequest;
import ru.silex.tasktracker.exception.TaskNotFoundException;
import ru.silex.tasktracker.service.TaskService;
import ru.silex.tasktracker.web.RestApiExceptionHandler;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TaskController.class)
@Import(RestApiExceptionHandler.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Test
    void given_validRequest_when_create_then_status201_and_locationAndBody() throws Exception {
        var created = new TaskResponse(1L, "Onboarding", "New hire checklist", TaskStatus.NEW);
        when(taskService.create(any(CreateTaskRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Onboarding\",\"description\":\"New hire checklist\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/api/tasks/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Onboarding"))
                .andExpect(jsonPath("$.description").value("New hire checklist"))
                .andExpect(jsonPath("$.status").value("NEW"));
    }

    @Test
    void given_titleAndNullDescription_when_create_then_status201() throws Exception {
        var created = new TaskResponse(2L, "Only title", null, TaskStatus.NEW);
        when(taskService.create(any(CreateTaskRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Only title\",\"description\":null}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.title").value("Only title"));
    }

    @Test
    void given_blankTitle_when_create_then_status400_andValidationErrors() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"   \",\"description\":\"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors", hasSize(1)))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("title"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("must not be blank"));

        verify(taskService, never()).create(any());
    }

    @Test
    void given_emptyJson_when_create_then_status400() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("title"));

        verify(taskService, never()).create(any());
    }

    @Test
    void given_titleExceedsMaxLength_when_create_then_status400() throws Exception {
        String longTitle = "x".repeat(501);
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + longTitle + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("title"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("must be at most 500 characters"));
    }

    @Test
    void given_descriptionExceedsMaxLength_when_create_then_status400() throws Exception {
        String longDescription = "y".repeat(5001);
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"ok\",\"description\":\"" + longDescription + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("description"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("must be at most 5000 characters"));
    }

    @Test
    void given_invalidJson_when_create_then_status400() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"bad"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed JSON or incompatible types"));
    }

    @Test
    void given_existingTask_when_getById_then_status200() throws Exception {
        var task = new TaskResponse(10L, "T", "D", TaskStatus.IN_PROGRESS);
        when(taskService.getById(10L)).thenReturn(task);

        mockMvc.perform(get("/api/tasks/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void given_unknownTask_when_getById_then_status404() throws Exception {
        when(taskService.getById(99L)).thenThrow(new TaskNotFoundException(99L));

        mockMvc.perform(get("/api/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found: id=99"))
                .andExpect(jsonPath("$.fieldErrors", hasSize(0)));
    }

    @Test
    void given_nonNumericId_when_getById_then_status400() throws Exception {
        mockMvc.perform(get("/api/tasks/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for path or query parameter 'id'"));
    }

    @Test
    void given_validStatus_when_patchStatus_then_status200() throws Exception {
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
    void given_unknownTask_when_patchStatus_then_status404() throws Exception {
        when(taskService.updateStatus(eq(7L), any(UpdateTaskStatusRequest.class)))
                .thenThrow(new TaskNotFoundException(7L));

        mockMvc.perform(patch("/api/tasks/7/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DONE\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found: id=7"));
    }

    @Test
    void given_invalidStatusLiteral_when_patchStatus_then_status400() throws Exception {
        mockMvc.perform(patch("/api/tasks/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INVALID_STATUS\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed JSON or incompatible types"));

        verify(taskService, never()).updateStatus(any(), any());
    }

    @Test
    void given_missingStatusField_when_patchStatus_then_status400() throws Exception {
        mockMvc.perform(patch("/api/tasks/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("status"));

        verify(taskService, never()).updateStatus(any(), any());
    }

    @Test
    void given_nonNumericId_when_patchStatus_then_status400() throws Exception {
        mockMvc.perform(patch("/api/tasks/not-id/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DONE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for path or query parameter 'id'"));
    }
}
