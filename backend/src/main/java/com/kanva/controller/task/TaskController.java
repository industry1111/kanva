package com.kanva.controller.task;

import com.kanva.common.response.ApiResponse;
import com.kanva.dto.task.TaskPositionUpdateRequest;
import com.kanva.dto.task.TaskRequest;
import com.kanva.dto.task.TaskResponse;
import com.kanva.dto.task.TaskStatusUpdateRequest;
import com.kanva.security.UserPrincipal;
import com.kanva.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /**
     * 특정 날짜의 Task 목록 조회
     * GET /api/tasks?date=2025-01-18
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksByDate(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam LocalDate date) {
        Long userId = principal.getId();

        List<TaskResponse> response = taskService.getTasksByDate(userId, date);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Task 단건 조회
     * GET /api/tasks/{taskId}
     */
    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponse>> getTask(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long taskId) {
        Long userId = principal.getId();

        TaskResponse response = taskService.getTask(userId, taskId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Task 생성
     * POST /api/tasks?date=2025-01-18
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam LocalDate date,
            @Valid @RequestBody TaskRequest request) {
        Long userId = principal.getId();

        TaskResponse response = taskService.createTask(userId, date, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    /**
     * Task 수정
     * PUT /api/tasks/{taskId}
     */
    @PutMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskRequest request) {
        Long userId = principal.getId();

        TaskResponse response = taskService.updateTask(userId, taskId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Task 상태 변경
     * PATCH /api/tasks/{taskId}/status
     */
    @PatchMapping("/{taskId}/status")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTaskStatus(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskStatusUpdateRequest request) {
        Long userId = principal.getId();

        TaskResponse response = taskService.updateTaskStatus(userId, taskId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Task 완료 상태 토글 (COMPLETED <-> PENDING)
     * PATCH /api/tasks/{taskId}/toggle
     */
    @PatchMapping("/{taskId}/toggle")
    public ResponseEntity<ApiResponse<TaskResponse>> toggleTask(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long taskId) {
        Long userId = principal.getId();

        TaskResponse response = taskService.toggleTask(userId, taskId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Task 삭제
     * DELETE /api/tasks/{taskId}
     */
    @DeleteMapping("/{taskId}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long taskId) {
        Long userId = principal.getId();

        taskService.deleteTask(userId, taskId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    /**
     * Task 순서 변경 (드래그앤드롭)
     * PUT /api/tasks/positions?date=2025-01-18
     */
    @PutMapping("/positions")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> updateTaskPositions(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam LocalDate date,
            @Valid @RequestBody TaskPositionUpdateRequest request) {
        Long userId = principal.getId();

        List<TaskResponse> response = taskService.updateTaskPositions(userId, date, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 마감 지난 Task 목록 조회
     * GET /api/tasks/overdue
     */
    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getOverdueTasks(
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal.getId();

        List<TaskResponse> response = taskService.getOverdueTasks(userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
