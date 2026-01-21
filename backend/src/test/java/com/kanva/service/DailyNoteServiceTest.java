package com.kanva.service;

import com.kanva.domain.dailynote.DailyNote;
import com.kanva.domain.dailynote.DailyNoteRepository;
import com.kanva.domain.user.Role;
import com.kanva.domain.user.User;
import com.kanva.domain.user.UserRepository;
import com.kanva.dto.dailynote.DailyNoteDetailResponse;
import com.kanva.dto.dailynote.DailyNoteRequest;
import com.kanva.dto.dailynote.DailyNoteResponse;
import com.kanva.dto.dailynote.DailyNoteSummaryResponse;
import com.kanva.exception.UserNotFoundException;
import com.kanva.service.impl.DailyNoteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DailyNoteService 테스트")
class DailyNoteServiceTest {

    @Mock
    private DailyNoteRepository dailyNoteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DailyNoteServiceImpl dailyNoteService;

    @Captor
    private ArgumentCaptor<DailyNote> dailyNoteCaptor;

    private Long userId;
    private User user;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        userId = 1L;
        user = createUser(userId, "test@example.com", "테스트유저");
        today = LocalDate.of(2025, 1, 18);
    }

    /**
     * 테스트용 User 객체 생성 헬퍼 메서드
     */
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

    /**
     * 테스트용 DailyNote 객체 생성 헬퍼 메서드
     * ReflectionTestUtils를 사용해 private 필드(id, createdAt, updatedAt) 설정
     */
    private DailyNote createDailyNote(Long id, User user, LocalDate date, String content) {
        DailyNote dailyNote = DailyNote.builder()
                .user(user)
                .date(date)
                .content(content)
                .build();
        ReflectionTestUtils.setField(dailyNote, "id", id);
        ReflectionTestUtils.setField(dailyNote, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(dailyNote, "updatedAt", LocalDateTime.now());
        return dailyNote;
    }

    @Nested
    @DisplayName("getOrCreateDailyNote 메서드")
    class GetOrCreateDailyNote {

        @Test
        @DisplayName("기존 DailyNote가 있으면 조회하여 반환한다")
        void getExistingDailyNote() {
            // given
            DailyNote existingNote = createDailyNote(1L, user, today, "기존 내용");

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(dailyNoteRepository.findByUserAndDate(user, today))
                    .willReturn(Optional.of(existingNote));

            // when
            DailyNoteDetailResponse response = dailyNoteService.getOrCreateDailyNote(userId, today);

            // then
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getDate()).isEqualTo(today);
            assertThat(response.getContent()).isEqualTo("기존 내용");

            verify(userRepository).findById(userId);
            verify(dailyNoteRepository).findByUserAndDate(user, today);
            verify(dailyNoteRepository, never()).save(any());
        }

        @Test
        @DisplayName("기존 DailyNote가 없으면 새로 생성하여 반환한다")
        void createNewDailyNote() {
            // given
            DailyNote newNote = createDailyNote(1L, user, today, null);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(dailyNoteRepository.findByUserAndDate(user, today))
                    .willReturn(Optional.empty());
            given(dailyNoteRepository.save(any(DailyNote.class)))
                    .willReturn(newNote);

            // when
            DailyNoteDetailResponse response = dailyNoteService.getOrCreateDailyNote(userId, today);

            // then
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getDate()).isEqualTo(today);
            assertThat(response.getContent()).isNull();

            verify(userRepository).findById(userId);
            verify(dailyNoteRepository).findByUserAndDate(user, today);
            verify(dailyNoteRepository).save(dailyNoteCaptor.capture());

            DailyNote capturedNote = dailyNoteCaptor.getValue();
            assertThat(capturedNote.getUser()).isEqualTo(user);
            assertThat(capturedNote.getDate()).isEqualTo(today);
            assertThat(capturedNote.getContent()).isNull();
        }

        @Test
        @DisplayName("존재하지 않는 userId로 조회 시 예외를 발생시킨다")
        void throwExceptionWhenUserNotFound() {
            // given
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> dailyNoteService.getOrCreateDailyNote(userId, today))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userRepository).findById(userId);
            verify(dailyNoteRepository, never()).findByUserAndDate(any(), any());
        }
    }

    @Nested
    @DisplayName("updateDailyNote 메서드")
    class UpdateDailyNote {

        @Test
        @DisplayName("기존 DailyNote의 content를 수정한다")
        void updateExistingDailyNote() {
            // given
            DailyNote existingNote = createDailyNote(1L, user, today, "원래 내용");

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(dailyNoteRepository.findByUserAndDate(user, today))
                    .willReturn(Optional.of(existingNote));

            DailyNoteRequest request = DailyNoteRequest.builder()
                    .date(today)
                    .content("수정된 내용")
                    .build();

            // when
            DailyNoteResponse response = dailyNoteService.updateDailyNote(userId, today, request);

            // then
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getContent()).isEqualTo("수정된 내용");

            verify(userRepository).findById(userId);
            verify(dailyNoteRepository).findByUserAndDate(user, today);
        }

        @Test
        @DisplayName("DailyNote가 없으면 새로 생성하고 content를 설정한다")
        void createAndUpdateDailyNote() {
            // given
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(dailyNoteRepository.findByUserAndDate(user, today))
                    .willReturn(Optional.empty());
            given(dailyNoteRepository.save(any(DailyNote.class)))
                    .willAnswer(invocation -> {
                        DailyNote note = invocation.getArgument(0);
                        ReflectionTestUtils.setField(note, "id", 1L);
                        ReflectionTestUtils.setField(note, "createdAt", LocalDateTime.now());
                        ReflectionTestUtils.setField(note, "updatedAt", LocalDateTime.now());
                        return note;
                    });

            DailyNoteRequest request = DailyNoteRequest.builder()
                    .date(today)
                    .content("새 내용")
                    .build();

            // when
            DailyNoteResponse response = dailyNoteService.updateDailyNote(userId, today, request);

            // then
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getContent()).isEqualTo("새 내용");

            verify(dailyNoteRepository).save(any(DailyNote.class));
        }

        @Test
        @DisplayName("content를 null로 수정할 수 있다")
        void updateContentToNull() {
            // given
            DailyNote existingNote = createDailyNote(1L, user, today, "원래 내용");

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(dailyNoteRepository.findByUserAndDate(user, today))
                    .willReturn(Optional.of(existingNote));

            DailyNoteRequest request = DailyNoteRequest.builder()
                    .date(today)
                    .content(null)
                    .build();

            // when
            DailyNoteResponse response = dailyNoteService.updateDailyNote(userId, today, request);

            // then
            assertThat(response.getContent()).isNull();
        }
    }

    @Nested
    @DisplayName("deleteDailyNote 메서드")
    class DeleteDailyNote {

        @Test
        @DisplayName("기존 DailyNote가 있으면 삭제한다")
        void deleteExistingDailyNote() {
            // given
            DailyNote existingNote = createDailyNote(1L, user, today, "삭제할 내용");

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(dailyNoteRepository.findByUserAndDate(user, today))
                    .willReturn(Optional.of(existingNote));

            // when
            dailyNoteService.deleteDailyNote(userId, today);

            // then
            verify(userRepository).findById(userId);
            verify(dailyNoteRepository).findByUserAndDate(user, today);
            verify(dailyNoteRepository).delete(existingNote);
        }

        @Test
        @DisplayName("DailyNote가 없으면 아무 동작도 하지 않는다")
        void deleteNonExistingDailyNote() {
            // given
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(dailyNoteRepository.findByUserAndDate(user, today))
                    .willReturn(Optional.empty());

            // when
            dailyNoteService.deleteDailyNote(userId, today);

            // then
            verify(userRepository).findById(userId);
            verify(dailyNoteRepository).findByUserAndDate(user, today);
            verify(dailyNoteRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("getMonthlyNotes 메서드")
    class GetMonthlyNotes {

        @Test
        @DisplayName("해당 월의 DailyNote 목록을 요약 형태로 반환한다")
        void getMonthlyNotesSummary() {
            // given
            YearMonth yearMonth = YearMonth.of(2025, 1);

            DailyNote note1 = createDailyNote(1L, user, LocalDate.of(2025, 1, 15), "내용 있음");
            DailyNote note2 = createDailyNote(2L, user, LocalDate.of(2025, 1, 18), null);
            DailyNote note3 = createDailyNote(3L, user, LocalDate.of(2025, 1, 20), "");

            given(dailyNoteRepository.findByUserIdAndDateRange(
                    eq(userId),
                    eq(LocalDate.of(2025, 1, 1)),
                    eq(LocalDate.of(2025, 1, 31))
            )).willReturn(List.of(note1, note2, note3));

            // when
            List<DailyNoteSummaryResponse> responses = dailyNoteService.getMonthlyNotes(userId, yearMonth);

            // then
            assertThat(responses).hasSize(3);

            // 내용 있는 노트
            assertThat(responses.get(0).getDate()).isEqualTo(LocalDate.of(2025, 1, 15));
            assertThat(responses.get(0).isHasContent()).isTrue();

            // 내용 없는 노트 (null)
            assertThat(responses.get(1).getDate()).isEqualTo(LocalDate.of(2025, 1, 18));
            assertThat(responses.get(1).isHasContent()).isFalse();

            // 내용 없는 노트 (빈 문자열)
            assertThat(responses.get(2).getDate()).isEqualTo(LocalDate.of(2025, 1, 20));
            assertThat(responses.get(2).isHasContent()).isFalse();
        }

        @Test
        @DisplayName("해당 월에 DailyNote가 없으면 빈 리스트를 반환한다")
        void getEmptyMonthlyNotes() {
            // given
            YearMonth yearMonth = YearMonth.of(2025, 1);

            given(dailyNoteRepository.findByUserIdAndDateRange(
                    eq(userId),
                    eq(LocalDate.of(2025, 1, 1)),
                    eq(LocalDate.of(2025, 1, 31))
            )).willReturn(List.of());

            // when
            List<DailyNoteSummaryResponse> responses = dailyNoteService.getMonthlyNotes(userId, yearMonth);

            // then
            assertThat(responses).isEmpty();
        }

        @Test
        @DisplayName("2월의 마지막 날을 올바르게 계산한다 (윤년)")
        void getFebruaryNotesLeapYear() {
            // given
            YearMonth yearMonth = YearMonth.of(2024, 2); // 2024년은 윤년

            given(dailyNoteRepository.findByUserIdAndDateRange(
                    eq(userId),
                    eq(LocalDate.of(2024, 2, 1)),
                    eq(LocalDate.of(2024, 2, 29)) // 윤년이므로 29일
            )).willReturn(List.of());

            // when
            dailyNoteService.getMonthlyNotes(userId, yearMonth);

            // then
            verify(dailyNoteRepository).findByUserIdAndDateRange(
                    userId,
                    LocalDate.of(2024, 2, 1),
                    LocalDate.of(2024, 2, 29)
            );
        }

        @Test
        @DisplayName("2월의 마지막 날을 올바르게 계산한다 (평년)")
        void getFebruaryNotesNonLeapYear() {
            // given
            YearMonth yearMonth = YearMonth.of(2025, 2); // 2025년은 평년

            given(dailyNoteRepository.findByUserIdAndDateRange(
                    eq(userId),
                    eq(LocalDate.of(2025, 2, 1)),
                    eq(LocalDate.of(2025, 2, 28)) // 평년이므로 28일
            )).willReturn(List.of());

            // when
            dailyNoteService.getMonthlyNotes(userId, yearMonth);

            // then
            verify(dailyNoteRepository).findByUserIdAndDateRange(
                    userId,
                    LocalDate.of(2025, 2, 1),
                    LocalDate.of(2025, 2, 28)
            );
        }
    }
}
