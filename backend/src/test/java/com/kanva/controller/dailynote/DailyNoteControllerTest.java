package com.kanva.controller.dailynote;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanva.dto.dailynote.DailyNoteDetailResponse;
import com.kanva.dto.dailynote.DailyNoteRequest;
import com.kanva.dto.dailynote.DailyNoteResponse;
import com.kanva.dto.dailynote.DailyNoteSummaryResponse;
import com.kanva.security.jwt.JwtAuthenticationFilter;
import com.kanva.security.jwt.JwtTokenProvider;
import com.kanva.service.DailyNoteService;
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
import java.time.YearMonth;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = DailyNoteController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("DailyNoteController 테스트")
class DailyNoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DailyNoteService dailyNoteService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private static final LocalDate TEST_DATE = LocalDate.of(2025, 1, 20);
    private static final LocalDateTime TEST_DATETIME = LocalDateTime.of(2025, 1, 18, 10, 0, 0);

    @Nested
    @DisplayName("GET /api/daily-notes/{date}")
    class GetDailyNote {

        @Test
        @DisplayName("날짜로 DailyNote를 조회한다")
        void getDailyNoteSuccess() throws Exception {
            // given
            DailyNoteDetailResponse response = DailyNoteDetailResponse.builder()
                    .id(1L)
                    .date(TEST_DATE)
                    .content("테스트 내용")
                    .createdAt(TEST_DATETIME)
                    .updatedAt(TEST_DATETIME)
                    .build();

            given(dailyNoteService.getOrCreateDailyNote(eq(1L), eq(TEST_DATE)))
                    .willReturn(response);

            // when & then
            mockMvc.perform(get("/api/daily-notes/{date}", "2025-01-20"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.date").value("2025-01-20"))
                    .andExpect(jsonPath("$.data.content").value("테스트 내용"))
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("조회 성공"));

            verify(dailyNoteService).getOrCreateDailyNote(1L, TEST_DATE);
        }

        @Test
        @DisplayName("content가 null인 DailyNote도 조회할 수 있다")
        void getDailyNoteWithNullContent() throws Exception {
            // given
            DailyNoteDetailResponse response = DailyNoteDetailResponse.builder()
                    .id(1L)
                    .date(TEST_DATE)
                    .content(null)
                    .createdAt(TEST_DATETIME)
                    .updatedAt(TEST_DATETIME)
                    .build();

            given(dailyNoteService.getOrCreateDailyNote(eq(1L), eq(TEST_DATE)))
                    .willReturn(response);

            // when & then
            mockMvc.perform(get("/api/daily-notes/{date}", "2025-01-20"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.content").doesNotExist());
        }

        @Test
        @DisplayName("잘못된 날짜 형식이면 400 에러를 반환한다")
        void getDailyNoteWithInvalidDateFormat() throws Exception {
            // when & then
            mockMvc.perform(get("/api/daily-notes/{date}", "invalid-date"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/daily-notes/{date}")
    class UpdateDailyNote {

        @Test
        @DisplayName("DailyNote의 content를 수정한다")
        void updateDailyNoteSuccess() throws Exception {
            // given
            DailyNoteRequest request = DailyNoteRequest.builder()
                    .date(TEST_DATE)
                    .content("수정된 내용")
                    .build();

            DailyNoteResponse response = DailyNoteResponse.builder()
                    .id(1L)
                    .date(TEST_DATE)
                    .content("수정된 내용")
                    .createdAt(TEST_DATETIME)
                    .updatedAt(TEST_DATETIME)
                    .build();

            given(dailyNoteService.updateDailyNote(eq(1L), eq(TEST_DATE), any(DailyNoteRequest.class)))
                    .willReturn(response);

            // when & then
            mockMvc.perform(put("/api/daily-notes/{date}", "2025-01-20")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.content").value("수정된 내용"))
                    .andExpect(jsonPath("$.code").value(200));

            verify(dailyNoteService).updateDailyNote(eq(1L), eq(TEST_DATE), any(DailyNoteRequest.class));
        }

        @Test
        @DisplayName("content가 null이어도 수정 가능하다")
        void updateDailyNoteWithNullContent() throws Exception {
            // given
            DailyNoteRequest request = DailyNoteRequest.builder()
                    .date(TEST_DATE)
                    .content(null)
                    .build();

            DailyNoteResponse response = DailyNoteResponse.builder()
                    .id(1L)
                    .date(TEST_DATE)
                    .content(null)
                    .createdAt(TEST_DATETIME)
                    .updatedAt(TEST_DATETIME)
                    .build();

            given(dailyNoteService.updateDailyNote(eq(1L), eq(TEST_DATE), any(DailyNoteRequest.class)))
                    .willReturn(response);

            // when & then
            mockMvc.perform(put("/api/daily-notes/{date}", "2025-01-20")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").doesNotExist());
        }

        @Test
        @DisplayName("긴 마크다운 content도 수정할 수 있다")
        void updateDailyNoteWithLongMarkdownContent() throws Exception {
            // given
            String markdownContent = """
                    # 오늘의 할 일

                    ## 업무
                    - [ ] 회의 참석
                    - [x] 코드 리뷰
                    - [ ] 문서 작성

                    ## 개인
                    - [ ] 운동하기
                    - [ ] 책 읽기

                    > 오늘도 화이팅!
                    """;

            DailyNoteRequest request = DailyNoteRequest.builder()
                    .date(TEST_DATE)
                    .content(markdownContent)
                    .build();

            DailyNoteResponse response = DailyNoteResponse.builder()
                    .id(1L)
                    .date(TEST_DATE)
                    .content(markdownContent)
                    .createdAt(TEST_DATETIME)
                    .updatedAt(TEST_DATETIME)
                    .build();

            given(dailyNoteService.updateDailyNote(eq(1L), eq(TEST_DATE), any(DailyNoteRequest.class)))
                    .willReturn(response);

            // when & then
            mockMvc.perform(put("/api/daily-notes/{date}", "2025-01-20")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").value(markdownContent));
        }

        @Test
        @DisplayName("Request Body가 없으면 400 에러를 반환한다")
        void updateDailyNoteWithoutBody() throws Exception {
            // when & then
            mockMvc.perform(put("/api/daily-notes/{date}", "2025-01-20")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("잘못된 날짜 형식이면 400 에러를 반환한다")
        void updateDailyNoteWithInvalidDateFormat() throws Exception {
            // given
            DailyNoteRequest request = DailyNoteRequest.builder()
                    .date(TEST_DATE)
                    .content("내용")
                    .build();

            // when & then
            mockMvc.perform(put("/api/daily-notes/{date}", "invalid-date")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /api/daily-notes/{date}")
    class DeleteDailyNote {

        @Test
        @DisplayName("DailyNote를 삭제한다")
        void deleteDailyNoteSuccess() throws Exception {
            // given
            doNothing().when(dailyNoteService).deleteDailyNote(eq(1L), eq(TEST_DATE));

            // when & then
            mockMvc.perform(delete("/api/daily-notes/{date}", "2025-01-20"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.code").value(200));

            verify(dailyNoteService).deleteDailyNote(1L, TEST_DATE);
        }

        @Test
        @DisplayName("잘못된 날짜 형식이면 400 에러를 반환한다")
        void deleteDailyNoteWithInvalidDateFormat() throws Exception {
            // when & then
            mockMvc.perform(delete("/api/daily-notes/{date}", "invalid-date"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/daily-notes/calendar")
    class GetMonthlyNotes {

        @Test
        @DisplayName("월별 DailyNote 요약 목록을 조회한다")
        void getMonthlyNotesSuccess() throws Exception {
            // given
            List<DailyNoteSummaryResponse> responses = List.of(
                    DailyNoteSummaryResponse.builder()
                            .date(LocalDate.of(2025, 1, 20))
                            .hasContent(true)
                            .build(),
                    DailyNoteSummaryResponse.builder()
                            .date(LocalDate.of(2025, 1, 25))
                            .hasContent(false)
                            .build()
            );

            given(dailyNoteService.getMonthlyNotes(eq(1L), eq(YearMonth.of(2025, 1))))
                    .willReturn(responses);

            // when & then
            mockMvc.perform(get("/api/daily-notes/calendar")
                            .param("month", "2025-01"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].date").value("2025-01-20"))
                    .andExpect(jsonPath("$.data[0].hasContent").value(true))
                    .andExpect(jsonPath("$.data[1].date").value("2025-01-25"))
                    .andExpect(jsonPath("$.data[1].hasContent").value(false))
                    .andExpect(jsonPath("$.code").value(200));

            verify(dailyNoteService).getMonthlyNotes(1L, YearMonth.of(2025, 1));
        }

        @Test
        @DisplayName("해당 월에 DailyNote가 없으면 빈 배열을 반환한다")
        void getEmptyMonthlyNotes() throws Exception {
            // given
            given(dailyNoteService.getMonthlyNotes(eq(1L), eq(YearMonth.of(2025, 2))))
                    .willReturn(List.of());

            // when & then
            mockMvc.perform(get("/api/daily-notes/calendar")
                            .param("month", "2025-02"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("month 파라미터가 없으면 400 에러를 반환한다")
        void getMonthlyNotesWithoutMonth() throws Exception {
            // when & then
            mockMvc.perform(get("/api/daily-notes/calendar"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("잘못된 month 형식이면 400 에러를 반환한다")
        void getMonthlyNotesWithInvalidMonthFormat() throws Exception {
            // when & then
            mockMvc.perform(get("/api/daily-notes/calendar")
                            .param("month", "2025/01"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }
}
