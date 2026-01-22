package com.kanva.controller.taskseries;

import com.kanva.common.response.ApiResponse;
import com.kanva.dto.taskseries.TaskSeriesRequest;
import com.kanva.dto.taskseries.TaskSeriesResponse;
import com.kanva.service.TaskSeriesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
            @Valid @RequestBody TaskSeriesRequest request) {
        // TODO: 임시 개발용 - 인증 구현 후 @AuthenticationPrincipal에서 userId 추출
        Long userId = 1L;

        TaskSeriesResponse response = taskSeriesService.createSeries(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    /**
     * 사용자의 모든 시리즈 목록 조회
     * GET /api/task-series
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskSeriesResponse>>> getUserSeries() {
        // TODO: 임시 개발용 - 인증 구현 후 @AuthenticationPrincipal에서 userId 추출
        Long userId = 1L;

        List<TaskSeriesResponse> response = taskSeriesService.getUserSeries(userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 사용자의 ACTIVE 시리즈 목록 조회
     * GET /api/task-series/active
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<TaskSeriesResponse>>> getUserActiveSeries() {
        // TODO: 임시 개발용 - 인증 구현 후 @AuthenticationPrincipal에서 userId 추출
        Long userId = 1L;

        List<TaskSeriesResponse> response = taskSeriesService.getUserActiveSeries(userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 수동으로 오늘 Task 생성 트리거 (테스트/디버그용)
     * POST /api/task-series/generate
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<String>> generateTodayTasks() {
        taskSeriesService.generateTodayTasks();
        return ResponseEntity.ok(ApiResponse.ok("오늘 Task 생성 완료"));
    }
}
