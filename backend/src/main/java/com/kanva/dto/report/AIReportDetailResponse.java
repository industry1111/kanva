package com.kanva.dto.report;

import com.kanva.domain.report.AIReport;
import com.kanva.domain.report.ReportFeedback;
import com.kanva.domain.report.ReportPeriodType;
import com.kanva.domain.report.ReportStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class AIReportDetailResponse {

    private Long id;
    private ReportPeriodType periodType;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private ReportStatus status;
    private Integer totalTasks;
    private Integer completedTasks;
    private Integer completionRate;
    private String trend;
    private String summary;
    private String insights;
    private String recommendations;
    private String errorMessage;
    private ReportFeedback feedback;
    private LocalDateTime createdAt;

    public static AIReportDetailResponse from(AIReport report) {
        return AIReportDetailResponse.builder()
                .id(report.getId())
                .periodType(report.getPeriodType())
                .periodStart(report.getPeriodStart())
                .periodEnd(report.getPeriodEnd())
                .status(report.getStatus())
                .totalTasks(report.getTotalTasks())
                .completedTasks(report.getCompletedTasks())
                .completionRate(report.getCompletionRate())
                .trend(report.getTrend())
                .summary(report.getSummary())
                .insights(report.getInsights())
                .recommendations(report.getRecommendations())
                .errorMessage(report.getErrorMessage())
                .feedback(report.getFeedback())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
