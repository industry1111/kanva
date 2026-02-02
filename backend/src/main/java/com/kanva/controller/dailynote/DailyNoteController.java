package com.kanva.controller.dailynote;

import com.kanva.common.response.ApiResponse;
import com.kanva.dto.dailynote.*;
import com.kanva.security.UserPrincipal;
import com.kanva.service.DailyNoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/daily-notes")
@RequiredArgsConstructor
public class DailyNoteController {

    private final DailyNoteService dailyNoteService;

    /**
     * 특정 날짜 노트 조회 (없으면 생성)
     * GET /api/daily-notes/{date}
     */
    @GetMapping("/{date}")
    public ResponseEntity<ApiResponse<DailyNoteDetailResponse>> getDailyNote(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long userId = principal.getId();

        DailyNoteDetailResponse response = dailyNoteService.getOrCreateDailyNote(userId, date);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 노트 수정
     * PUT /api/daily-notes/{date}
     */
    @PutMapping("/{date}")
    public ResponseEntity<ApiResponse<DailyNoteResponse>> updateDailyNote(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody DailyNoteRequest request) {
        Long userId = principal.getId();

        DailyNoteResponse response = dailyNoteService.updateDailyNote(userId, date, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 노트 삭제
     * DELETE /api/daily-notes/{date}
     */
    @DeleteMapping("/{date}")
    public ResponseEntity<ApiResponse<Void>> deleteDailyNote(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long userId = principal.getId();

        dailyNoteService.deleteDailyNote(userId, date);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    /**
     * 월별 노트 목록 조회
     * GET /api/daily-notes/calendar?month=2025-01
     */
    @GetMapping("/calendar")
    public ResponseEntity<ApiResponse<List<DailyNoteSummaryResponse>>> getMonthlyNotes(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam YearMonth month) {
        Long userId = principal.getId();

        List<DailyNoteSummaryResponse> response = dailyNoteService.getMonthlyNotes(userId, month);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
