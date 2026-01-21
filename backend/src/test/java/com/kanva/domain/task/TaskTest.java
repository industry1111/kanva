package com.kanva.domain.task;

import com.kanva.domain.dailynote.DailyNote;
import com.kanva.domain.user.Role;
import com.kanva.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Task 엔티티 테스트")
class TaskTest {

    private User testUser;
    private DailyNote testDailyNote;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .password("password123")
                .name("테스트유저")
                .role(Role.USER)
                .build();

        testDailyNote = DailyNote.builder()
                .user(testUser)
                .date(LocalDate.of(2025, 1, 18))
                .content("오늘의 노트")
                .build();
    }

    @Nested
    @DisplayName("Task 생성")
    class CreateTask {

        @Test
        @DisplayName("Builder로 Task를 생성할 수 있다")
        void createWithBuilder() {
            // given
            String title = "API 개발";
            String description = "REST API 엔드포인트 구현";
            LocalDate dueDate = LocalDate.of(2025, 1, 20);
            TaskStatus status = TaskStatus.IN_PROGRESS;
            Integer position = 1;

            // when
            Task task = Task.builder()
                    .dailyNote(testDailyNote)
                    .title(title)
                    .description(description)
                    .dueDate(dueDate)
                    .status(status)
                    .position(position)
                    .build();

            // then
            assertThat(task.getDailyNote()).isEqualTo(testDailyNote);
            assertThat(task.getTitle()).isEqualTo(title);
            assertThat(task.getDescription()).isEqualTo(description);
            assertThat(task.getDueDate()).isEqualTo(dueDate);
            assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
            assertThat(task.getPosition()).isEqualTo(1);
        }

        @Test
        @DisplayName("status를 지정하지 않으면 기본값 PENDING으로 설정된다")
        void createWithDefaultStatus() {
            // when
            Task task = Task.builder()
                    .dailyNote(testDailyNote)
                    .title("테스트 작업")
                    .status(null)
                    .build();

            // then
            assertThat(task.getStatus()).isEqualTo(TaskStatus.PENDING);
        }

        @Test
        @DisplayName("position을 지정하지 않으면 기본값 0으로 설정된다")
        void createWithDefaultPosition() {
            // when
            Task task = Task.builder()
                    .dailyNote(testDailyNote)
                    .title("테스트 작업")
                    .position(null)
                    .build();

            // then
            assertThat(task.getPosition()).isEqualTo(0);
        }

        @Test
        @DisplayName("description과 dueDate가 null인 Task를 생성할 수 있다")
        void createWithNullOptionalFields() {
            // when
            Task task = Task.builder()
                    .dailyNote(testDailyNote)
                    .title("테스트 작업")
                    .description(null)
                    .dueDate(null)
                    .build();

            // then
            assertThat(task.getDescription()).isNull();
            assertThat(task.getDueDate()).isNull();
        }
    }

    @Nested
    @DisplayName("Task 상태 변경")
    class UpdateTaskStatus {

        @Test
        @DisplayName("상태를 IN_PROGRESS로 변경할 수 있다")
        void start() {
            // given
            Task task = Task.builder()
                    .dailyNote(testDailyNote)
                    .title("테스트 작업")
                    .status(TaskStatus.PENDING)
                    .build();

            // when
            task.start();

            // then
            assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
            assertThat(task.isInProgress()).isTrue();
        }

        @Test
        @DisplayName("상태를 COMPLETED로 변경할 수 있다")
        void complete() {
            // given
            Task task = Task.builder()
                    .dailyNote(testDailyNote)
                    .title("테스트 작업")
                    .status(TaskStatus.IN_PROGRESS)
                    .build();

            // when
            task.complete();

            // then
            assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED);
            assertThat(task.isCompleted()).isTrue();
        }

        @Test
        @DisplayName("상태를 PENDING으로 되돌릴 수 있다")
        void pending() {
            // given
            Task task = Task.builder()
                    .dailyNote(testDailyNote)
                    .title("테스트 작업")
                    .status(TaskStatus.COMPLETED)
                    .build();

            // when
            task.pending();

            // then
            assertThat(task.getStatus()).isEqualTo(TaskStatus.PENDING);
            assertThat(task.isPending()).isTrue();
        }

        @Test
        @DisplayName("updateStatus로 상태를 직접 변경할 수 있다")
        void updateStatus() {
            // given
            Task task = Task.builder()
                    .dailyNote(testDailyNote)
                    .title("테스트 작업")
                    .status(TaskStatus.PENDING)
                    .build();

            // when
            task.updateStatus(TaskStatus.COMPLETED);

            // then
            assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        }
    }

    @Nested
    @DisplayName("Task 정보 수정")
    class UpdateTaskInfo {

        @Test
        @DisplayName("title을 수정할 수 있다")
        void updateTitle() {
            // given
            Task task = Task.builder()
                    .dailyNote(testDailyNote)
                    .title("원래 제목")
                    .build();

            // when
            task.updateTitle("수정된 제목");

            // then
            assertThat(task.getTitle()).isEqualTo("수정된 제목");
        }

        @Test
        @DisplayName("description을 수정할 수 있다")
        void updateDescription() {
            // given
            Task task = Task.builder()
                    .dailyNote(testDailyNote)
                    .title("테스트 작업")
                    .description("원래 설명")
                    .build();

            // when
            task.updateDescription("수정된 설명");

            // then
            assertThat(task.getDescription()).isEqualTo("수정된 설명");
        }

        @Test
        @DisplayName("dueDate를 수정할 수 있다")
        void updateDueDate() {
            // given
            Task task = Task.builder()
                    .dailyNote(testDailyNote)
                    .title("테스트 작업")
                    .dueDate(LocalDate.of(2025, 1, 20))
                    .build();

            // when
            task.updateDueDate(LocalDate.of(2025, 1, 25));

            // then
            assertThat(task.getDueDate()).isEqualTo(LocalDate.of(2025, 1, 25));
        }

        @Test
        @DisplayName("position을 수정할 수 있다")
        void updatePosition() {
            // given
            Task task = Task.builder()
                    .dailyNote(testDailyNote)
                    .title("테스트 작업")
                    .position(0)
                    .build();

            // when
            task.updatePosition(5);

            // then
            assertThat(task.getPosition()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Task 편의 메서드")
    class TaskUtilityMethods {

        @Test
        @DisplayName("마감일이 지났고 완료되지 않은 Task는 isOverdue가 true이다")
        void isOverdue_true() {
            // given
            Task task = Task.builder()
                    .dailyNote(testDailyNote)
                    .title("지난 작업")
                    .dueDate(LocalDate.now().minusDays(1))
                    .status(TaskStatus.PENDING)
                    .build();

            // when & then
            assertThat(task.isOverdue()).isTrue();
        }

        @Test
        @DisplayName("마감일이 지났지만 완료된 Task는 isOverdue가 false이다")
        void isOverdue_false_whenCompleted() {
            // given
            Task task = Task.builder()
                    .dailyNote(testDailyNote)
                    .title("완료된 작업")
                    .dueDate(LocalDate.now().minusDays(1))
                    .status(TaskStatus.COMPLETED)
                    .build();

            // when & then
            assertThat(task.isOverdue()).isFalse();
        }

        @Test
        @DisplayName("마감일이 없는 Task는 isOverdue가 false이다")
        void isOverdue_false_whenNoDueDate() {
            // given
            Task task = Task.builder()
                    .dailyNote(testDailyNote)
                    .title("마감일 없는 작업")
                    .dueDate(null)
                    .status(TaskStatus.PENDING)
                    .build();

            // when & then
            assertThat(task.isOverdue()).isFalse();
        }

        @Test
        @DisplayName("마감일이 오늘인 Task는 isOverdue가 false이다")
        void isOverdue_false_whenDueDateIsToday() {
            // given
            Task task = Task.builder()
                    .dailyNote(testDailyNote)
                    .title("오늘 마감 작업")
                    .dueDate(LocalDate.now())
                    .status(TaskStatus.PENDING)
                    .build();

            // when & then
            assertThat(task.isOverdue()).isFalse();
        }
    }

    @Nested
    @DisplayName("TaskStatus enum")
    class TaskStatusEnum {

        @Test
        @DisplayName("PENDING의 설명을 조회할 수 있다")
        void getPendingDescription() {
            assertThat(TaskStatus.PENDING.getDescription()).isEqualTo("대기");
        }

        @Test
        @DisplayName("IN_PROGRESS의 설명을 조회할 수 있다")
        void getInProgressDescription() {
            assertThat(TaskStatus.IN_PROGRESS.getDescription()).isEqualTo("진행 중");
        }

        @Test
        @DisplayName("COMPLETED의 설명을 조회할 수 있다")
        void getCompletedDescription() {
            assertThat(TaskStatus.COMPLETED.getDescription()).isEqualTo("완료");
        }
    }
}
