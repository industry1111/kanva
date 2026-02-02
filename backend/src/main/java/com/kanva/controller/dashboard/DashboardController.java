package com.kanva.controller.dashboard;

import com.kanva.common.response.ApiResponse;
import com.kanva.dto.dashboard.DashboardResponse;
import com.kanva.security.UserPrincipal;
import com.kanva.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 대시보드 데이터 조회 (월 기준)
     * GET /api/dashboard?month=2026-01
     */
    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam String month) {
        Long userId = principal.getId();

        YearMonth yearMonth = YearMonth.parse(month);
        DashboardResponse response = dashboardService.getDashboard(userId, yearMonth);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
