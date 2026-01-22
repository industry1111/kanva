package com.kanva.controller.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanva.domain.task.TaskStatus;
import com.kanva.dto.task.TaskPositionUpdateRequest;
import com.kanva.dto.task.TaskRequest;
import com.kanva.dto.task.TaskResponse;
import com.kanva.dto.task.TaskStatusUpdateRequest;
import com.kanva.security.jwt.JwtAuthenticationFilter;
import com.kanva.security.jwt.JwtTokenProvider;
import com.kanva.service.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = TaskController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("TaskController 테스트")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private static final LocalDate TEST_DATE = LocalDate.of(2025, 1, 18);
    private static final LocalDateTime TEST_DATETIME = LocalDateTime.of(2025, 1, 18, 10, 0, 0);

    private TaskResponse createTaskResponse(Long id, String title, TaskStatus status, int position) {
        return TaskResponse.builder()
                .id(id)
                .dailyNoteId(1L)
                .title(title)
                .description("설명")
                .status(status)
                .position(position)
                .overdue(false)
                .createdAt(TEST_DATETIME)
                .updatedAt(TEST_DATETIME)
                .build();
    }

    @Nested
    @DisplayName("GET /api/tasks")
    class GetTasksByDate {

        @Test
        @DisplayName("특정 날짜의 Task 목록을 조회한다")
        void success() throws Exception {
            // given
            List<TaskResponse> responses = List.of(
                    createTaskResponse(1L, "작업 1", TaskStatus.PENDING, 0),
                    createTaskResponse(2L, "작업 2", TaskStatus.IN_PROGRESS, 1)
            );

            given(taskService.getTasksByDate(eq(1L), eq(TEST_DATE)))
                    .willReturn(responses);

            // when & then
            mockMvc.perform(get("/api/tasks")
                            .param("date", "2025-01-18"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].title").value("작업 1"))
                    .andExpect(jsonPath("$.data[1].title").value("작업 2"))
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("날짜 파라미터가 없으면 400 에러를 반환한다")
        void badRequest_withoutDate() throws Exception {
            mockMvc.perform(get("/api/tasks"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/tasks/{taskId}")
    class GetTask {

        @Test
        @DisplayName("Task를 조회한다")
        void success() throws Exception {
            // given
            TaskResponse response = createTaskResponse(1L, "작업 1", TaskStatus.PENDING, 0);

            given(taskService.getTask(eq(1L), eq(1L)))
                    .willReturn(response);

            // when & then
            mockMvc.perform(get("/api/tasks/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.title").value("작업 1"))
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("POST /api/tasks")
    class CreateTask {

        @Test
        @DisplayName("Task를 생성한다")
        void success() throws Exception {
            // given
            TaskRequest request = TaskRequest.builder()
                    .title("새 작업")
                    .description("설명")
                    .status(TaskStatus.PENDING)
                    .build();

            TaskResponse response = createTaskResponse(1L, "새 작업", TaskStatus.PENDING, 0);

            given(taskService.createTask(eq(1L), eq(TEST_DATE), any(TaskRequest.class)))
                    .willReturn(response);

            // when & then
            mockMvc.perform(post("/api/tasks")
                            .param("date", "2025-01-18")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.title").value("새 작업"))
                    .andExpect(jsonPath("$.code").value(201));
        }

        @Test
        @DisplayName("제목이 없으면 400 에러를 반환한다")
        void badRequest_withoutTitle() throws Exception {
            // given
            TaskRequest request = TaskRequest.builder()
                    .title(null)
                    .build();

            // when & then
            mockMvc.perform(post("/api/tasks")
                            .param("date", "2025-01-18")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/tasks/{taskId}")
    class UpdateTask {

        @Test
        @DisplayName("Task를 수정한다")
        void success() throws Exception {
            // given
            TaskRequest request = TaskRequest.builder()
                    .title("수정된 작업")
                    .description("수정된 설명")
                    .status(TaskStatus.IN_PROGRESS)
                    .build();

            TaskResponse response = TaskResponse.builder()
                    .id(1L)
                    .dailyNoteId(1L)
                    .title("수정된 작업")
                    .description("수정된 설명")
                    .status(TaskStatus.IN_PROGRESS)
                    .position(0)
                    .overdue(false)
                    .createdAt(TEST_DATETIME)
                    .updatedAt(TEST_DATETIME)
                    .build();

            given(taskService.updateTask(eq(1L), eq(1L), any(TaskRequest.class)))
                    .willReturn(response);

            // when & then
            mockMvc.perform(put("/api/tasks/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.title").value("수정된 작업"))
                    .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/tasks/{taskId}/status")
    class UpdateTaskStatus {

        @Test
        @DisplayName("Task 상태를 변경한다")
        void success() throws Exception {
            // given
            TaskStatusUpdateRequest request = TaskStatusUpdateRequest.builder()
                    .status(TaskStatus.COMPLETED)
                    .build();

            TaskResponse response = createTaskResponse(1L, "작업 1", TaskStatus.COMPLETED, 0);

            given(taskService.updateTaskStatus(eq(1L), eq(1L), any(TaskStatusUpdateRequest.class)))
                    .willReturn(response);

            // when & then
            mockMvc.perform(patch("/api/tasks/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("COMPLETED"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/tasks/{taskId}/toggle")
    class ToggleTask {

        @Test
        @DisplayName("PENDING 상태의 Task를 COMPLETED로 토글한다")
        void togglePendingToCompleted() throws Exception {
            // given
            TaskResponse response = createTaskResponse(1L, "작업 1", TaskStatus.COMPLETED, 0);

            given(taskService.toggleTask(eq(1L), eq(1L)))
                    .willReturn(response);

            // when & then
            mockMvc.perform(patch("/api/tasks/1/toggle"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.code").value(200));

            verify(taskService).toggleTask(1L, 1L);
        }

        @Test
        @DisplayName("COMPLETED 상태의 Task를 PENDING으로 토글한다")
        void toggleCompletedToPending() throws Exception {
            // given
            TaskResponse response = createTaskResponse(1L, "작업 1", TaskStatus.PENDING, 0);

            given(taskService.toggleTask(eq(1L), eq(1L)))
                    .willReturn(response);

            // when & then
            mockMvc.perform(patch("/api/tasks/1/toggle"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("PENDING"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/tasks/{taskId}")
    class DeleteTask {

        @Test
        @DisplayName("Task를 삭제한다")
        void success() throws Exception {
            // given
            doNothing().when(taskService).deleteTask(eq(1L), eq(1L));

            // when & then
            mockMvc.perform(delete("/api/tasks/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.code").value(200));

            verify(taskService).deleteTask(1L, 1L);
        }
    }

    @Nested
    @DisplayName("PUT /api/tasks/positions")
    class UpdateTaskPositions {

        @Test
        @DisplayName("Task 순서를 변경한다")
        void success() throws Exception {
            // given
            TaskPositionUpdateRequest request = TaskPositionUpdateRequest.builder()
                    .taskIds(List.of(3L, 1L, 2L))
                    .build();

            List<TaskResponse> responses = List.of(
                    createTaskResponse(3L, "작업 3", TaskStatus.PENDING, 0),
                    createTaskResponse(1L, "작업 1", TaskStatus.PENDING, 1),
                    createTaskResponse(2L, "작업 2", TaskStatus.PENDING, 2)
            );

            given(taskService.updateTaskPositions(eq(1L), eq(TEST_DATE), any(TaskPositionUpdateRequest.class)))
                    .willReturn(responses);

            // when & then
            mockMvc.perform(put("/api/tasks/positions")
                            .param("date", "2025-01-18")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(3))
                    .andExpect(jsonPath("$.data[0].position").value(0))
                    .andExpect(jsonPath("$.data[1].position").value(1))
                    .andExpect(jsonPath("$.data[2].position").value(2));
        }
    }

    @Nested
    @DisplayName("GET /api/tasks/overdue")
    class GetOverdueTasks {

        @Test
        @DisplayName("마감 지난 Task 목록을 조회한다")
        void success() throws Exception {
            // given
            TaskResponse overdueTask = TaskResponse.builder()
                    .id(1L)
                    .dailyNoteId(1L)
                    .title("마감 지난 작업")
                    .status(TaskStatus.PENDING)
                    .position(0)
                    .overdue(true)
                    .createdAt(TEST_DATETIME)
                    .updatedAt(TEST_DATETIME)
                    .build();

            given(taskService.getOverdueTasks(eq(1L)))
                    .willReturn(List.of(overdueTask));

            // when & then
            mockMvc.perform(get("/api/tasks/overdue"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].overdue").value(true));
        }
    }
}
