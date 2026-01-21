package com.kanva.domain.task;

import com.kanva.domain.dailynote.DailyNote;
import com.kanva.domain.user.Role;
import com.kanva.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("TaskRepository 테스트")
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TestEntityManager em;

    private User user;
    private DailyNote dailyNote;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("test@example.com")
                .password("password123")
                .name("테스트유저")
                .role(Role.USER)
                .build();
        em.persist(user);

        today = LocalDate.of(2025, 1, 18);

        dailyNote = DailyNote.builder()
                .user(user)
                .date(today)
                .content("오늘의 노트")
                .build();
        em.persist(dailyNote);
    }

    @Nested
    @DisplayName("findByDailyNoteOrderByPositionAsc 메서드")
    class FindByDailyNoteOrderByPositionAsc {

        @Test
        @DisplayName("DailyNote로 Task 목록을 position 오름차순으로 조회한다")
        void success() {
            // given
            Task task1 = Task.builder()
                    .dailyNote(dailyNote)
                    .title("세 번째 작업")
                    .position(2)
                    .build();
            Task task2 = Task.builder()
                    .dailyNote(dailyNote)
                    .title("첫 번째 작업")
                    .position(0)
                    .build();
            Task task3 = Task.builder()
                    .dailyNote(dailyNote)
                    .title("두 번째 작업")
                    .position(1)
                    .build();

            em.persist(task1);
            em.persist(task2);
            em.persist(task3);
            em.flush();
            em.clear();

            // when
            List<Task> found = taskRepository.findByDailyNoteOrderByPositionAsc(dailyNote);

            // then
            assertThat(found).hasSize(3);
            assertThat(found.get(0).getTitle()).isEqualTo("첫 번째 작업");
            assertThat(found.get(1).getTitle()).isEqualTo("두 번째 작업");
            assertThat(found.get(2).getTitle()).isEqualTo("세 번째 작업");
        }

        @Test
        @DisplayName("Task가 없으면 빈 리스트를 반환한다")
        void emptyList() {
            // when
            List<Task> found = taskRepository.findByDailyNoteOrderByPositionAsc(dailyNote);

            // then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByUserIdAndDate 메서드")
    class FindByUserIdAndDate {

        @Test
        @DisplayName("userId와 date로 Task 목록을 조회한다")
        void success() {
            // given
            Task task1 = Task.builder()
                    .dailyNote(dailyNote)
                    .title("작업 1")
                    .position(0)
                    .build();
            Task task2 = Task.builder()
                    .dailyNote(dailyNote)
                    .title("작업 2")
                    .position(1)
                    .build();

            em.persist(task1);
            em.persist(task2);
            em.flush();
            em.clear();

            // when
            List<Task> found = taskRepository.findByUserIdAndDate(user.getId(), today);

            // then
            assertThat(found).hasSize(2);
            assertThat(found).extracting("title")
                    .containsExactly("작업 1", "작업 2");
        }

        @Test
        @DisplayName("다른 날짜의 Task는 조회되지 않는다")
        void notFoundByDate() {
            // given
            Task task = Task.builder()
                    .dailyNote(dailyNote)
                    .title("오늘 작업")
                    .position(0)
                    .build();
            em.persist(task);
            em.flush();
            em.clear();

            // when
            List<Task> found = taskRepository.findByUserIdAndDate(user.getId(), today.plusDays(1));

            // then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByUserIdAndStatus 메서드")
    class FindByUserIdAndStatus {

        @Test
        @DisplayName("userId와 status로 Task 목록을 조회한다")
        void success() {
            // given
            Task pendingTask = Task.builder()
                    .dailyNote(dailyNote)
                    .title("대기 작업")
                    .status(TaskStatus.PENDING)
                    .position(0)
                    .build();
            Task inProgressTask = Task.builder()
                    .dailyNote(dailyNote)
                    .title("진행 중 작업")
                    .status(TaskStatus.IN_PROGRESS)
                    .position(1)
                    .build();
            Task completedTask = Task.builder()
                    .dailyNote(dailyNote)
                    .title("완료 작업")
                    .status(TaskStatus.COMPLETED)
                    .position(2)
                    .build();

            em.persist(pendingTask);
            em.persist(inProgressTask);
            em.persist(completedTask);
            em.flush();
            em.clear();

            // when
            List<Task> found = taskRepository.findByUserIdAndStatus(user.getId(), TaskStatus.IN_PROGRESS);

            // then
            assertThat(found).hasSize(1);
            assertThat(found.get(0).getTitle()).isEqualTo("진행 중 작업");
        }
    }

    @Nested
    @DisplayName("findOverdueTasks 메서드")
    class FindOverdueTasks {

        @Test
        @DisplayName("마감일이 지난 미완료 Task를 조회한다")
        void success() {
            // given
            Task overdueTask = Task.builder()
                    .dailyNote(dailyNote)
                    .title("마감 지난 작업")
                    .dueDate(LocalDate.now().minusDays(1))
                    .status(TaskStatus.PENDING)
                    .position(0)
                    .build();
            Task completedOverdueTask = Task.builder()
                    .dailyNote(dailyNote)
                    .title("완료된 작업")
                    .dueDate(LocalDate.now().minusDays(1))
                    .status(TaskStatus.COMPLETED)
                    .position(1)
                    .build();
            Task futureTask = Task.builder()
                    .dailyNote(dailyNote)
                    .title("미래 작업")
                    .dueDate(LocalDate.now().plusDays(1))
                    .status(TaskStatus.PENDING)
                    .position(2)
                    .build();

            em.persist(overdueTask);
            em.persist(completedOverdueTask);
            em.persist(futureTask);
            em.flush();
            em.clear();

            // when
            List<Task> found = taskRepository.findOverdueTasks(user.getId(), LocalDate.now());

            // then
            assertThat(found).hasSize(1);
            assertThat(found.get(0).getTitle()).isEqualTo("마감 지난 작업");
        }
    }

    @Nested
    @DisplayName("countByDailyNoteId 메서드")
    class CountByDailyNoteId {

        @Test
        @DisplayName("DailyNote의 Task 개수를 조회한다")
        void success() {
            // given
            Task task1 = Task.builder()
                    .dailyNote(dailyNote)
                    .title("작업 1")
                    .position(0)
                    .build();
            Task task2 = Task.builder()
                    .dailyNote(dailyNote)
                    .title("작업 2")
                    .position(1)
                    .build();

            em.persist(task1);
            em.persist(task2);
            em.flush();
            em.clear();

            // when
            int count = taskRepository.countByDailyNoteId(dailyNote.getId());

            // then
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("Task가 없으면 0을 반환한다")
        void zeroCount() {
            // when
            int count = taskRepository.countByDailyNoteId(dailyNote.getId());

            // then
            assertThat(count).isZero();
        }
    }

    @Nested
    @DisplayName("countCompletedByDailyNoteId 메서드")
    class CountCompletedByDailyNoteId {

        @Test
        @DisplayName("완료된 Task 개수를 조회한다")
        void success() {
            // given
            Task completedTask1 = Task.builder()
                    .dailyNote(dailyNote)
                    .title("완료 작업 1")
                    .status(TaskStatus.COMPLETED)
                    .position(0)
                    .build();
            Task completedTask2 = Task.builder()
                    .dailyNote(dailyNote)
                    .title("완료 작업 2")
                    .status(TaskStatus.COMPLETED)
                    .position(1)
                    .build();
            Task pendingTask = Task.builder()
                    .dailyNote(dailyNote)
                    .title("대기 작업")
                    .status(TaskStatus.PENDING)
                    .position(2)
                    .build();

            em.persist(completedTask1);
            em.persist(completedTask2);
            em.persist(pendingTask);
            em.flush();
            em.clear();

            // when
            int count = taskRepository.countCompletedByDailyNoteId(dailyNote.getId());

            // then
            assertThat(count).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("findMaxPositionByDailyNoteId 메서드")
    class FindMaxPositionByDailyNoteId {

        @Test
        @DisplayName("최대 position 값을 조회한다")
        void success() {
            // given
            Task task1 = Task.builder()
                    .dailyNote(dailyNote)
                    .title("작업 1")
                    .position(0)
                    .build();
            Task task2 = Task.builder()
                    .dailyNote(dailyNote)
                    .title("작업 2")
                    .position(5)
                    .build();
            Task task3 = Task.builder()
                    .dailyNote(dailyNote)
                    .title("작업 3")
                    .position(3)
                    .build();

            em.persist(task1);
            em.persist(task2);
            em.persist(task3);
            em.flush();
            em.clear();

            // when
            Integer maxPosition = taskRepository.findMaxPositionByDailyNoteId(dailyNote.getId());

            // then
            assertThat(maxPosition).isEqualTo(5);
        }

        @Test
        @DisplayName("Task가 없으면 null을 반환한다")
        void nullWhenEmpty() {
            // when
            Integer maxPosition = taskRepository.findMaxPositionByDailyNoteId(dailyNote.getId());

            // then
            assertThat(maxPosition).isNull();
        }
    }

    @Nested
    @DisplayName("save 메서드")
    class Save {

        @Test
        @DisplayName("Task를 저장하면 ID가 생성된다")
        void saveGeneratesId() {
            // given
            Task task = Task.builder()
                    .dailyNote(dailyNote)
                    .title("테스트 작업")
                    .position(0)
                    .build();

            // when
            Task saved = taskRepository.save(task);

            // then
            assertThat(saved.getId()).isNotNull();
        }

        @Test
        @DisplayName("Task를 저장하면 createdAt과 updatedAt이 자동 설정된다")
        void saveGeneratesAuditFields() {
            // given
            Task task = Task.builder()
                    .dailyNote(dailyNote)
                    .title("테스트 작업")
                    .position(0)
                    .build();

            // when
            Task saved = taskRepository.save(task);
            em.flush();

            // then
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getUpdatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("delete 메서드")
    class Delete {

        @Test
        @DisplayName("Task를 삭제할 수 있다")
        void deleteSuccess() {
            // given
            Task task = Task.builder()
                    .dailyNote(dailyNote)
                    .title("삭제할 작업")
                    .position(0)
                    .build();
            em.persist(task);
            em.flush();
            em.clear();

            Long savedId = task.getId();

            // when
            Task toDelete = taskRepository.findById(savedId).orElseThrow();
            taskRepository.delete(toDelete);
            em.flush();
            em.clear();

            // then
            Optional<Task> found = taskRepository.findById(savedId);
            assertThat(found).isEmpty();
        }
    }
}
