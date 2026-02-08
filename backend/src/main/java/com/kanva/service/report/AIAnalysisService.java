package com.kanva.service.report;

import com.kanva.domain.dailynote.DailyNote;
import com.kanva.domain.report.ReportPeriodType;
import com.kanva.domain.task.Task;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public interface AIAnalysisService {

    @Getter
    @Builder
    class AnalysisResult {
        private Integer totalTasks;
        private Integer completedTasks;
        private Integer completionRate;
        private String trend;
        private String summary;
        private String insights;
        private String recommendations;
    }

    @Getter
    @Builder
    class AnalysisContext {
        private List<Task> currentTasks;
        private List<Task> previousPeriodTasks;
        private List<DailyNote> dailyNotes;
        private ReportPeriodType periodType;
        private String tone; // ENCOURAGING or STRICT
        private Integer previousReportCompletionRate; // 이전 리포트의 완료율 (이전 기간 Task가 없을 때 사용)
    }

    AnalysisResult analyze(AnalysisContext context);
}
