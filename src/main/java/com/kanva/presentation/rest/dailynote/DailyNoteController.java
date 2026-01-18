package com.kanva.presentation.rest.dailynote;

import com.kanva.common.response.ApiResponse;
import com.kanva.dto.dailynote.*;
import com.kanva.service.DailyNoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/daily-notes")
@RequiredArgsConstructor
public class DailyNoteController {

    private final DailyNoteService dailyNoteService;

    @GetMapping
    public ResponseEntity<ApiResponse<DailyNoteDetailResponse>> getDailyNote(
            @RequestParam LocalDate date) {
        // TODO: JWT 인증 후 userId 추출
        Long userId = 1L;

        DailyNoteDetailResponse response = dailyNoteService.getOrCreateDailyNote(userId, date);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<DailyNoteResponse>> updateDailyNote(
            @RequestParam LocalDate date,
            @Valid @RequestBody DailyNoteRequest request) {
        // TODO: JWT 인증 후 userId 추출
        Long userId = 1L;

        DailyNoteResponse response = dailyNoteService.updateDailyNote(userId, date, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteDailyNote(@RequestParam LocalDate date) {
        // TODO: JWT 인증 후 userId 추출
        Long userId = 1L;

        dailyNoteService.deleteDailyNote(userId, date);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/calendar")
    public ResponseEntity<ApiResponse<List<DailyNoteSummaryResponse>>> getMonthlyNotes(
            @RequestParam YearMonth month) {
        // TODO: JWT 인증 후 userId 추출
        Long userId = 1L;

        List<DailyNoteSummaryResponse> response = dailyNoteService.getMonthlyNotes(userId, month);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
