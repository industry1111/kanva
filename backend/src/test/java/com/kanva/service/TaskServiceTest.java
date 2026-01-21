package com.kanva.service;

import com.kanva.domain.dailynote.DailyNote;
import com.kanva.domain.dailynote.DailyNoteRepository;
import com.kanva.domain.task.Task;
import com.kanva.domain.task.TaskRepository;
import com.kanva.domain.task.TaskStatus;
import com.kanva.domain.user.Role;
import com.kanva.domain.user.User;
import com.kanva.domain.user.UserRepository;
import com.kanva.dto.task.TaskPositionUpdateRequest;
import com.kanva.dto.task.TaskRequest;
import com.kanva.dto.task.TaskResponse;
import com.kanva.dto.task.TaskStatusUpdateRequest;
import com.kanva.exception.TaskNotFoundException;
import com.kanva.exception.UserNotFoundException;
import com.kanva.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService 테스트")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private DailyNoteRepository dailyNoteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Long userId;
    private User user;
    private DailyNote dailyNote;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        userId = 1L;
        user = createUser(userId, "test@example.com", "테스트유저");
        today = LocalDate.of(2025, 1, 18);
        dailyNote = createDailyNote(1L, user, today);
    }

    private User createUser(Long id, String email, String name) {
        User user = User.builder()
                .email(email)
                .password("password123")
                .name(name)
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(user, "updatedAt", LocalDateTime.now());
        return user;
    }

    private DailyNote createDailyNote(Long id, User user, LocalDate date) {
        DailyNote dailyNote = DailyNote.builder()
                .user(user)
                .date(date)
                .content(null)
                .build();
        ReflectionTestUtils.setField(dailyNote, "id", id);
        ReflectionTestUtils.setField(dailyNote, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(dailyNote, "updatedAt", LocalDateTime.now());
        return dailyNote;
    }

    private Task createTask(Long id, DailyNote dailyNote, String title, TaskStatus status, int position) {
        Task task = Task.builder()
                .dailyNote(dailyNote)
                .title(title)
                .status(status)
                .position(position)
                .build();
        ReflectionTestUtils.setField(task, "id", id);
        ReflectionTestUtils.setField(task, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(task, "updatedAt", LocalDateTime.now());
        return task;
    }

    @Nested
    @DisplayName("getTasksByDate 메서드")
    class GetTasksByDate {

        @Test
        @DisplayName("특정 날짜의 Task 목록을 조회한다")
        void success() {
            // given
            Task task1 = createTask(1L, dailyNote, "작업 1", TaskStatus.PENDING, 0);
            Task task2 = createTask(2L, dailyNote, "작업 2", TaskStatus.IN_PROGRESS, 1);

            given(taskRepository.findByUserIdAndDate(userId, today))
                    .willReturn(List.of(task1, task2));

            // when
            List<TaskResponse> responses = taskService.getTasksByDate(userId, today);

            // then
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getTitle()).isEqualTo("작업 1");
            assertThat(responses.get(1).getTitle()).isEqualTo("작업 2");
        }

        @Test
        @DisplayName("Task가 없으면 빈 목록을 반환한다")
        void emptyList() {
            // given
            given(taskRepository.findByUserIdAndDate(userId, today))
                    .willReturn(List.of());

            // when
            List<TaskResponse> responses = taskService.getTasksByDate(userId, today);

            // then
            assertThat(responses).isEmpty();
        }
    }

    @Nested
    @DisplayName("getTask 메서드")
    class GetTask {

        @Test
        @DisplayName("Task를 조회한다")
        void success() {
            // given
            Task task = createTask(1L, dailyNote, "작업 1", TaskStatus.PENDING, 0);

            given(taskRepository.findById(1L)).willReturn(Optional.of(task));

            // when
            TaskResponse response = taskService.getTask(userId, 1L);

            // then
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getTitle()).isEqualTo("작업 1");
        }

        @Test
        @DisplayName("존재하지 않는 Task 조회 시 예외 발생")
        void notFound() {
            // given
            given(taskRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> taskService.getTask(userId, 999L))
                    .isInstanceOf(TaskNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("createTask 메서드")
    class CreateTask {

        @Test
        @DisplayName("Task를 생성한다")
        void success() {
            // given
            TaskRequest request = TaskRequest.builder()
                    .title("새 작업")
                    .description("설명")
                    .dueDate(LocalDate.of(2025, 1, 20))
                    .status(TaskStatus.PENDING)
                    .build();

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(dailyNoteRepository.findByUserAndDate(user, today))
                    .willReturn(Optional.of(dailyNote));
            given(taskRepository.findMaxPositionByDailyNoteId(dailyNote.getId()))
                    .willReturn(2);
            given(taskRepository.save(any(Task.class)))
                    .willAnswer(invocation -> {
                        Task task = invocation.getArgument(0);
                        ReflectionTestUtils.setField(task, "id", 1L);
                        ReflectionTestUtils.setField(task, "createdAt", LocalDateTime.now());
                        ReflectionTestUtils.setField(task, "updatedAt", LocalDateTime.now());
                        return task;
                    });

            // when
            TaskResponse response = taskService.createTask(userId, today, request);

            // then
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getTitle()).isEqualTo("새 작업");
            assertThat(response.getPosition()).isEqualTo(3); // maxPosition + 1
        }

        @Test
        @DisplayName("DailyNote가 없으면 새로 생성한다")
        void createDailyNoteIfNotExists() {
            // given
            TaskRequest request = TaskRequest.builder()
                    .title("새 작업")
                    .build();

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(dailyNoteRepository.findByUserAndDate(user, today))
                    .willReturn(Optional.empty());
            given(dailyNoteRepository.save(any(DailyNote.class)))
                    .willReturn(dailyNote);
            given(taskRepository.findMaxPositionByDailyNoteId(dailyNote.getId()))
                    .willReturn(null);
            given(taskRepository.save(any(Task.class)))
                    .willAnswer(invocation -> {
                        Task task = invocation.getArgument(0);
                        ReflectionTestUtils.setField(task, "id", 1L);
                        ReflectionTestUtils.setField(task, "createdAt", LocalDateTime.now());
                        ReflectionTestUtils.setField(task, "updatedAt", LocalDateTime.now());
                        return task;
                    });

            // when
            TaskResponse response = taskService.createTask(userId, today, request);

            // then
            assertThat(response.getPosition()).isEqualTo(0); // 첫 Task
            verify(dailyNoteRepository).save(any(DailyNote.class));
        }

        @Test
        @DisplayName("존재하지 않는 User로 생성 시 예외 발생")
        void userNotFound() {
            // given
            TaskRequest request = TaskRequest.builder()
                    .title("새 작업")
                    .build();

            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> taskService.createTask(userId, today, request))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateTask 메서드")
    class UpdateTask {

        @Test
        @DisplayName("Task를 수정한다")
        void success() {
            // given
            Task task = createTask(1L, dailyNote, "원래 제목", TaskStatus.PENDING, 0);

            TaskRequest request = TaskRequest.builder()
                    .title("수정된 제목")
                    .description("수정된 설명")
                    .dueDate(LocalDate.of(2025, 1, 25))
                    .status(TaskStatus.IN_PROGRESS)
                    .build();

            given(taskRepository.findById(1L)).willReturn(Optional.of(task));

            // when
            TaskResponse response = taskService.updateTask(userId, 1L, request);

            // then
            assertThat(response.getTitle()).isEqualTo("수정된 제목");
            assertThat(response.getDescription()).isEqualTo("수정된 설명");
            assertThat(response.getDueDate()).isEqualTo(LocalDate.of(2025, 1, 25));
            assertThat(response.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        }
    }

    @Nested
    @DisplayName("updateTaskStatus 메서드")
    class UpdateTaskStatus {

        @Test
        @DisplayName("Task 상태를 변경한다")
        void success() {
            // given
            Task task = createTask(1L, dailyNote, "작업", TaskStatus.PENDING, 0);

            TaskStatusUpdateRequest request = TaskStatusUpdateRequest.builder()
                    .status(TaskStatus.COMPLETED)
                    .build();

            given(taskRepository.findById(1L)).willReturn(Optional.of(task));

            // when
            TaskResponse response = taskService.updateTaskStatus(userId, 1L, request);

            // then
            assertThat(response.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        }
    }

    @Nested
    @DisplayName("deleteTask 메서드")
    class DeleteTask {

        @Test
        @DisplayName("Task를 삭제한다")
        void success() {
            // given
            Task task = createTask(1L, dailyNote, "삭제할 작업", TaskStatus.PENDING, 0);

            given(taskRepository.findById(1L)).willReturn(Optional.of(task));

            // when
            taskService.deleteTask(userId, 1L);

            // then
            verify(taskRepository).delete(task);
        }
    }

    @Nested
    @DisplayName("updateTaskPositions 메서드")
    class UpdateTaskPositions {

        @Test
        @DisplayName("Task 순서를 변경한다")
        void success() {
            // given
            Task task1 = createTask(1L, dailyNote, "작업 1", TaskStatus.PENDING, 0);
            Task task2 = createTask(2L, dailyNote, "작업 2", TaskStatus.PENDING, 1);
            Task task3 = createTask(3L, dailyNote, "작업 3", TaskStatus.PENDING, 2);

            TaskPositionUpdateRequest request = TaskPositionUpdateRequest.builder()
                    .taskIds(List.of(3L, 1L, 2L)) // 순서 변경
                    .build();

            given(taskRepository.findById(3L)).willReturn(Optional.of(task3));
            given(taskRepository.findById(1L)).willReturn(Optional.of(task1));
            given(taskRepository.findById(2L)).willReturn(Optional.of(task2));
            given(taskRepository.findByUserIdAndDate(userId, today))
                    .willReturn(List.of(task3, task1, task2));

            // when
            List<TaskResponse> responses = taskService.updateTaskPositions(userId, today, request);

            // then
            assertThat(task3.getPosition()).isEqualTo(0);
            assertThat(task1.getPosition()).isEqualTo(1);
            assertThat(task2.getPosition()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("getOverdueTasks 메서드")
    class GetOverdueTasks {

        @Test
        @DisplayName("마감 지난 Task 목록을 조회한다")
        void success() {
            // given
            Task overdueTask = createTask(1L, dailyNote, "마감 지난 작업", TaskStatus.PENDING, 0);
            ReflectionTestUtils.setField(overdueTask, "dueDate", LocalDate.now().minusDays(1));

            given(taskRepository.findOverdueTasks(userId, LocalDate.now()))
                    .willReturn(List.of(overdueTask));

            // when
            List<TaskResponse> responses = taskService.getOverdueTasks(userId);

            // then
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getTitle()).isEqualTo("마감 지난 작업");
        }
    }
}
