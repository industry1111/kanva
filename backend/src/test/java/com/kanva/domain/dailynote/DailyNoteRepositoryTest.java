package com.kanva.domain.dailynote;

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
@DisplayName("DailyNoteRepository 테스트")
class DailyNoteRepositoryTest {

    @Autowired
    private DailyNoteRepository dailyNoteRepository;

    @Autowired
    private TestEntityManager em;

    private User user;
    private User otherUser;
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

        otherUser = User.builder()
                .email("other@example.com")
                .password("password123")
                .name("다른유저")
                .role(Role.USER)
                .build();
        em.persist(otherUser);

        today = LocalDate.of(2025, 1, 18);
    }

    @Nested
    @DisplayName("findByUserAndDate 메서드")
    class FindByUserAndDate {

        @Test
        @DisplayName("User와 date로 DailyNote를 조회할 수 있다")
        void success() {
            // given
            DailyNote dailyNote = DailyNote.builder()
                    .user(user)
                    .date(today)
                    .content("테스트 내용")
                    .build();
            em.persist(dailyNote);
            em.flush();
            em.clear();

            // when
            Optional<DailyNote> found = dailyNoteRepository.findByUserAndDate(user, today);

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getUser().getId()).isEqualTo(user.getId());
            assertThat(found.get().getDate()).isEqualTo(today);
            assertThat(found.get().getContent()).isEqualTo("테스트 내용");
        }

        @Test
        @DisplayName("존재하지 않는 날짜로 조회 시 빈 Optional을 반환한다")
        void notFound_byDate() {
            // given
            DailyNote dailyNote = DailyNote.builder()
                    .user(user)
                    .date(today)
                    .content("테스트 내용")
                    .build();
            em.persist(dailyNote);
            em.flush();
            em.clear();

            LocalDate otherDate = today.plusDays(1);

            // when
            Optional<DailyNote> found = dailyNoteRepository.findByUserAndDate(user, otherDate);

            // then
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 User로 조회 시 빈 Optional을 반환한다")
        void notFound_byUser() {
            // given
            DailyNote dailyNote = DailyNote.builder()
                    .user(user)
                    .date(today)
                    .content("테스트 내용")
                    .build();
            em.persist(dailyNote);
            em.flush();
            em.clear();

            // when
            Optional<DailyNote> found = dailyNoteRepository.findByUserAndDate(otherUser, today);

            // then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByUserIdAndDate 메서드")
    class FindByUserIdAndDate {

        @Test
        @DisplayName("userId와 date로 DailyNote를 조회할 수 있다")
        void success() {
            // given
            DailyNote dailyNote = DailyNote.builder()
                    .user(user)
                    .date(today)
                    .content("테스트 내용")
                    .build();
            em.persist(dailyNote);
            em.flush();
            em.clear();

            // when
            Optional<DailyNote> found = dailyNoteRepository.findByUserIdAndDate(user.getId(), today);

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getUserId()).isEqualTo(user.getId());
            assertThat(found.get().getDate()).isEqualTo(today);
            assertThat(found.get().getContent()).isEqualTo("테스트 내용");
        }
    }

    @Nested
    @DisplayName("findByUser 메서드")
    class FindByUser {

        @Test
        @DisplayName("User로 모든 DailyNote를 조회할 수 있다")
        void success() {
            // given
            DailyNote note1 = DailyNote.builder()
                    .user(user)
                    .date(today)
                    .content("첫번째 노트")
                    .build();
            DailyNote note2 = DailyNote.builder()
                    .user(user)
                    .date(today.plusDays(1))
                    .content("두번째 노트")
                    .build();
            DailyNote otherUserNote = DailyNote.builder()
                    .user(otherUser)
                    .date(today)
                    .content("다른 사용자 노트")
                    .build();

            em.persist(note1);
            em.persist(note2);
            em.persist(otherUserNote);
            em.flush();
            em.clear();

            // when
            List<DailyNote> found = dailyNoteRepository.findByUser(user);

            // then
            assertThat(found).hasSize(2);
            assertThat(found).extracting("content")
                    .containsExactlyInAnyOrder("첫번째 노트", "두번째 노트");
        }

        @Test
        @DisplayName("DailyNote가 없는 User로 조회 시 빈 리스트를 반환한다")
        void emptyList() {
            // given
            User newUser = User.builder()
                    .email("new@example.com")
                    .password("password")
                    .name("새유저")
                    .role(Role.USER)
                    .build();
            em.persist(newUser);

            // when
            List<DailyNote> found = dailyNoteRepository.findByUser(newUser);

            // then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByUserIdAndDateRange 메서드")
    class FindByUserIdAndDateRange {

        @Test
        @DisplayName("기간 내 DailyNote를 날짜 내림차순으로 조회한다")
        void success() {
            // given
            DailyNote note1 = DailyNote.builder()
                    .user(user)
                    .date(LocalDate.of(2025, 1, 15))
                    .content("15일 노트")
                    .build();
            DailyNote note2 = DailyNote.builder()
                    .user(user)
                    .date(LocalDate.of(2025, 1, 18))
                    .content("18일 노트")
                    .build();
            DailyNote note3 = DailyNote.builder()
                    .user(user)
                    .date(LocalDate.of(2025, 1, 20))
                    .content("20일 노트")
                    .build();
            DailyNote outsideRangeNote = DailyNote.builder()
                    .user(user)
                    .date(LocalDate.of(2025, 2, 1))
                    .content("범위 밖 노트")
                    .build();

            em.persist(note1);
            em.persist(note2);
            em.persist(note3);
            em.persist(outsideRangeNote);
            em.flush();
            em.clear();

            LocalDate startDate = LocalDate.of(2025, 1, 1);
            LocalDate endDate = LocalDate.of(2025, 1, 31);

            // when
            List<DailyNote> found = dailyNoteRepository.findByUserIdAndDateRange(
                    user.getId(), startDate, endDate);

            // then
            assertThat(found).hasSize(3);
            // 날짜 내림차순 정렬 확인
            assertThat(found.get(0).getDate()).isEqualTo(LocalDate.of(2025, 1, 20));
            assertThat(found.get(1).getDate()).isEqualTo(LocalDate.of(2025, 1, 18));
            assertThat(found.get(2).getDate()).isEqualTo(LocalDate.of(2025, 1, 15));
        }

        @Test
        @DisplayName("기간 경계 날짜도 포함하여 조회한다")
        void inclusiveDateRange() {
            // given
            LocalDate startDate = LocalDate.of(2025, 1, 1);
            LocalDate endDate = LocalDate.of(2025, 1, 31);

            DailyNote startNote = DailyNote.builder()
                    .user(user)
                    .date(startDate)
                    .content("시작일 노트")
                    .build();
            DailyNote endNote = DailyNote.builder()
                    .user(user)
                    .date(endDate)
                    .content("종료일 노트")
                    .build();

            em.persist(startNote);
            em.persist(endNote);
            em.flush();
            em.clear();

            // when
            List<DailyNote> found = dailyNoteRepository.findByUserIdAndDateRange(
                    user.getId(), startDate, endDate);

            // then
            assertThat(found).hasSize(2);
            assertThat(found).extracting("date")
                    .containsExactlyInAnyOrder(startDate, endDate);
        }

        @Test
        @DisplayName("기간 내 DailyNote가 없으면 빈 리스트를 반환한다")
        void emptyInRange() {
            // given
            DailyNote note = DailyNote.builder()
                    .user(user)
                    .date(LocalDate.of(2025, 2, 15))
                    .content("2월 노트")
                    .build();
            em.persist(note);
            em.flush();
            em.clear();

            LocalDate startDate = LocalDate.of(2025, 1, 1);
            LocalDate endDate = LocalDate.of(2025, 1, 31);

            // when
            List<DailyNote> found = dailyNoteRepository.findByUserIdAndDateRange(
                    user.getId(), startDate, endDate);

            // then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByUserIdAndDate 메서드")
    class ExistsByUserIdAndDate {

        @Test
        @DisplayName("해당 날짜에 DailyNote가 존재하면 true를 반환한다")
        void exists() {
            // given
            DailyNote dailyNote = DailyNote.builder()
                    .user(user)
                    .date(today)
                    .content("테스트 내용")
                    .build();
            em.persist(dailyNote);
            em.flush();
            em.clear();

            // when
            boolean exists = dailyNoteRepository.existsByUserIdAndDate(user.getId(), today);

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("해당 날짜에 DailyNote가 존재하지 않으면 false를 반환한다")
        void notExists() {
            // when
            boolean exists = dailyNoteRepository.existsByUserIdAndDate(user.getId(), today);

            // then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("save 메서드")
    class Save {

        @Test
        @DisplayName("DailyNote를 저장하면 ID가 생성된다")
        void saveGeneratesId() {
            // given
            DailyNote dailyNote = DailyNote.builder()
                    .user(user)
                    .date(today)
                    .content("테스트 내용")
                    .build();

            // when
            DailyNote saved = dailyNoteRepository.save(dailyNote);

            // then
            assertThat(saved.getId()).isNotNull();
        }

        @Test
        @DisplayName("DailyNote를 저장하면 createdAt과 updatedAt이 자동 설정된다")
        void saveGeneratesAuditFields() {
            // given
            DailyNote dailyNote = DailyNote.builder()
                    .user(user)
                    .date(today)
                    .content("테스트 내용")
                    .build();

            // when
            DailyNote saved = dailyNoteRepository.save(dailyNote);
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
        @DisplayName("DailyNote를 삭제할 수 있다")
        void deleteSuccess() {
            // given
            DailyNote dailyNote = DailyNote.builder()
                    .user(user)
                    .date(today)
                    .content("테스트 내용")
                    .build();
            em.persist(dailyNote);
            em.flush();
            em.clear();

            Long savedId = dailyNote.getId();

            // when
            DailyNote toDelete = dailyNoteRepository.findById(savedId).orElseThrow();
            dailyNoteRepository.delete(toDelete);
            em.flush();
            em.clear();

            // then
            Optional<DailyNote> found = dailyNoteRepository.findById(savedId);
            assertThat(found).isEmpty();
        }
    }
}
