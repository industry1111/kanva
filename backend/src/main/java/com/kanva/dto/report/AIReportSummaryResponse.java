package com.kanva.dto.report;

import com.kanva.domain.report.AIReport;
import com.kanva.domain.report.ReportPeriodType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class AIReportSummaryResponse {

    private Long id;
    private ReportPeriodType periodType;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private Integer completionRate;
    private String trend;
    private String summary;
    private boolean hasReport;

    public static AIReportSummaryResponse from(AIReport report) {
        return AIReportSummaryResponse.builder()
                .id(report.getId())
                .periodType(report.getPeriodType())
                .periodStart(report.getPeriodStart())
                .periodEnd(report.getPeriodEnd())
                .completionRate(report.getCompletionRate())
                .trend(report.getTrend())
                .summary(report.getSummary())
                .hasReport(true)
                .build();
    }

    public static AIReportSummaryResponse empty() {
        return AIReportSummaryResponse.builder()
                .hasReport(false)
                .build();
    }
}
