package com.kanva.dto.report;

import com.kanva.domain.report.ReportPeriodType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class AIReportRequest {

    @NotNull(message = "기간 유형은 필수입니다.")
    private ReportPeriodType periodType;

    private LocalDate periodStart;

    private LocalDate periodEnd;
}
