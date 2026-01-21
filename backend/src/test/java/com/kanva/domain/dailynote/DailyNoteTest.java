package com.kanva.domain.dailynote;

import com.kanva.domain.user.Role;
import com.kanva.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DailyNote 엔티티 테스트")
class DailyNoteTest {

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .password("password123")
                .name("테스트유저")
                .role(Role.USER)
                .build();
    }

    @Nested
    @DisplayName("DailyNote 생성")
    class CreateDailyNote {

        @Test
        @DisplayName("Builder로 DailyNote를 생성할 수 있다")
        void createWithBuilder() {
            // given
            LocalDate date = LocalDate.of(2025, 1, 18);
            String content = "오늘의 노트 내용";

            // when
            DailyNote dailyNote = DailyNote.builder()
                    .user(testUser)
                    .date(date)
                    .content(content)
                    .build();

            // then
            assertThat(dailyNote.getUser()).isEqualTo(testUser);
            assertThat(dailyNote.getDate()).isEqualTo(date);
            assertThat(dailyNote.getContent()).isEqualTo(content);
        }

        @Test
        @DisplayName("content가 null인 빈 DailyNote를 생성할 수 있다")
        void createEmptyDailyNote() {
            // given
            LocalDate date = LocalDate.of(2025, 1, 18);

            // when
            DailyNote dailyNote = DailyNote.builder()
                    .user(testUser)
                    .date(date)
                    .content(null)
                    .build();

            // then
            assertThat(dailyNote.getUser()).isEqualTo(testUser);
            assertThat(dailyNote.getDate()).isEqualTo(date);
            assertThat(dailyNote.getContent()).isNull();
        }
    }

    @Nested
    @DisplayName("DailyNote 수정")
    class UpdateDailyNote {

        @Test
        @DisplayName("content를 수정할 수 있다")
        void updateContent() {
            // given
            DailyNote dailyNote = DailyNote.builder()
                    .user(testUser)
                    .date(LocalDate.of(2025, 1, 18))
                    .content("원래 내용")
                    .build();

            // when
            dailyNote.updateContent("수정된 내용");

            // then
            assertThat(dailyNote.getContent()).isEqualTo("수정된 내용");
        }

        @Test
        @DisplayName("content를 null로 수정할 수 있다")
        void updateContentToNull() {
            // given
            DailyNote dailyNote = DailyNote.builder()
                    .user(testUser)
                    .date(LocalDate.of(2025, 1, 18))
                    .content("원래 내용")
                    .build();

            // when
            dailyNote.updateContent(null);

            // then
            assertThat(dailyNote.getContent()).isNull();
        }

        @Test
        @DisplayName("content를 빈 문자열로 수정할 수 있다")
        void updateContentToEmpty() {
            // given
            DailyNote dailyNote = DailyNote.builder()
                    .user(testUser)
                    .date(LocalDate.of(2025, 1, 18))
                    .content("원래 내용")
                    .build();

            // when
            dailyNote.updateContent("");

            // then
            assertThat(dailyNote.getContent()).isEmpty();
        }
    }
}
