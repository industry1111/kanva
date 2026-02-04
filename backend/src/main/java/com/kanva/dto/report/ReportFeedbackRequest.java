package com.kanva.dto.report;

import com.kanva.domain.report.ReportFeedback;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReportFeedbackRequest {

    @NotNull(message = "피드백은 필수입니다.")
    private ReportFeedback feedback;
}
