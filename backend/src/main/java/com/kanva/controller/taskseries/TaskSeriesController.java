package com.kanva.controller.taskseries;

import com.kanva.common.response.ApiResponse;
import com.kanva.dto.taskseries.TaskSeriesRequest;
import com.kanva.dto.taskseries.TaskSeriesResponse;
import com.kanva.security.UserPrincipal;
import com.kanva.service.TaskSeriesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/task-series")
@RequiredArgsConstructor
public class TaskSeriesController {

    private final TaskSeriesService taskSeriesService;

    /**
     * 반복 Task 시리즈 생성
     * POST /api/task-series
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TaskSeriesResponse>> createSeries(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody TaskSeriesRequest request) {
        Long userId = principal.getId();

        TaskSeriesResponse response = taskSeriesService.createSeries(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    /**
     * 사용자의 모든 시리즈 목록 조회
     * GET /api/task-series
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskSeriesResponse>>> getUserSeries(
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal.getId();

        List<TaskSeriesResponse> response = taskSeriesService.getUserSeries(userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 사용자의 ACTIVE 시리즈 목록 조회
     * GET /api/task-series/active
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<TaskSeriesResponse>>> getUserActiveSeries(
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal.getId();

        List<TaskSeriesResponse> response = taskSeriesService.getUserActiveSeries(userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 시리즈에서 특정 날짜 제외
     * POST /api/task-series/{id}/exclude
     */
    @PostMapping("/{id}/exclude")
    public ResponseEntity<ApiResponse<String>> excludeDate(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        LocalDate date = LocalDate.parse(request.get("date"));
        taskSeriesService.excludeDate(id, date);
        return ResponseEntity.ok(ApiResponse.ok("날짜 제외 완료"));
    }

    /**
     * 시리즈 중단
     * POST /api/task-series/{id}/stop
     */
    @PostMapping("/{id}/stop")
    public ResponseEntity<ApiResponse<String>> stopSeries(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        LocalDate stopDate = LocalDate.parse(request.get("stopDate"));
        taskSeriesService.stopSeries(id, stopDate);
        return ResponseEntity.ok(ApiResponse.ok("시리즈 중단 완료"));
    }
}
