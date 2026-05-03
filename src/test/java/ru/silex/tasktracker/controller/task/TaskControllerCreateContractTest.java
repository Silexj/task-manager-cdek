package ru.silex.tasktracker.controller.task;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.silex.tasktracker.controller.TaskController;
import ru.silex.tasktracker.domain.TaskStatus;
import ru.silex.tasktracker.dto.CreateTaskRequest;
import ru.silex.tasktracker.dto.TaskResponse;
import ru.silex.tasktracker.service.TaskService;
import ru.silex.tasktracker.web.RestApiExceptionHandler;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract: {@code POST /api/tasks} — создание задачи (валидация тела + ответ 201).
 */
@DisplayName("Task API — создание (POST /api/tasks)")
@WebMvcTest(controllers = TaskController.class)
@Import(RestApiExceptionHandler.class)
class TaskControllerCreateContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Test
    void given_validRequest_when_post_then_201_location_and_body() throws Exception {
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
    void given_nullDescription_when_post_then_201() throws Exception {
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
    void given_blankTitle_when_post_then_400_and_service_not_called() throws Exception {
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
    void given_emptyBody_when_post_then_400() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("title"));

        verify(taskService, never()).create(any());
    }

    @Test
    void given_titleTooLong_when_post_then_400() throws Exception {
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
    void given_descriptionTooLong_when_post_then_400() throws Exception {
        String longDescription = "y".repeat(5001);
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"ok\",\"description\":\"" + longDescription + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("description"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("must be at most 5000 characters"));
    }

    @Test
    void given_malformedJson_when_post_then_400() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"bad"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed JSON or incompatible types"));
    }
}
