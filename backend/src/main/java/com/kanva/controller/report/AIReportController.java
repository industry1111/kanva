package com.kanva.controller.report;

import com.kanva.common.response.ApiResponse;
import com.kanva.dto.report.*;
import com.kanva.security.UserPrincipal;
import com.kanva.service.AIReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class AIReportController {

    private final AIReportService aiReportService;

    /**
     * 새 리포트 생성 (온디맨드)
     * POST /api/reports
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AIReportResponse>> generateReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AIReportRequest request) {
        Long userId = principal.getId();
        AIReportResponse response = aiReportService.generateReport(
                userId,
                request.getPeriodType(),
                request.getPeriodStart(),
                request.getPeriodEnd(),
                request.getTone()
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 리포트 상세 조회
     * GET /api/reports/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AIReportDetailResponse>> getReportDetail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        Long userId = principal.getId();
        AIReportDetailResponse response = aiReportService.getReportDetail(userId, id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 히스토리 목록 (페이징)
     * GET /api/reports?page=0&size=10
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AIReportResponse>>> getReportHistory(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long userId = principal.getId();
        Page<AIReportResponse> response = aiReportService.getReportHistory(userId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 피드백 제출
     * POST /api/reports/{id}/feedback
     */
    @PostMapping("/{id}/feedback")
    public ResponseEntity<ApiResponse<Void>> submitFeedback(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody ReportFeedbackRequest request) {
        Long userId = principal.getId();
        aiReportService.submitFeedback(userId, id, request.getFeedback());
        return ResponseEntity.ok(ApiResponse.ok());
    }

    /**
     * 리포트 삭제
     * DELETE /api/reports/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        Long userId = principal.getId();
        aiReportService.deleteReport(userId, id);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
