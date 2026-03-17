package com.kanva.controller.calendar;

import com.kanva.common.response.ApiResponse;
import com.kanva.dto.calendar.CalendarResponse;
import com.kanva.security.UserPrincipal;
import com.kanva.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    /**
     * 월별 캘린더 태스크 조회
     * GET /api/calendar?month=2026-03
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CalendarResponse>> getMonthlyTasks(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam String month) {
        Long userId = principal.getId();

        YearMonth yearMonth = YearMonth.parse(month);
        CalendarResponse response = calendarService.getMonthlyTasks(userId, yearMonth);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
