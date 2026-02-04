package com.kanva.service;

import com.kanva.domain.report.ReportFeedback;
import com.kanva.domain.report.ReportPeriodType;
import com.kanva.dto.report.AIReportDetailResponse;
import com.kanva.dto.report.AIReportResponse;
import com.kanva.dto.report.AIReportSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface AIReportService {

    /**
     * Dashboard용 주간 요약 조회 (최신 완료된 주간 리포트)
     */
    AIReportSummaryResponse getWeeklySummary(Long userId);

    /**
     * 새 리포트 생성 (온디맨드)
     */
    AIReportResponse generateReport(Long userId, ReportPeriodType periodType,
                                    LocalDate periodStart, LocalDate periodEnd);

    /**
     * 리포트 상세 조회
     */
    AIReportDetailResponse getReportDetail(Long userId, Long reportId);

    /**
     * 리포트 히스토리 목록 (페이징)
     */
    Page<AIReportResponse> getReportHistory(Long userId, Pageable pageable);

    /**
     * 피드백 제출
     */
    void submitFeedback(Long userId, Long reportId, ReportFeedback feedback);
}
