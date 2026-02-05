package com.kanva.service.report;

import com.kanva.domain.task.Task;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * AI 분석 서비스 인터페이스
 */
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

    /**
     * Task 데이터 기반 분석 수행
     *
     * @param tasks               현재 기간 Task 목록
     * @param previousPeriodTasks 이전 기간 Task 목록 (비교용)
     * @return 분석 결과
     */
    AnalysisResult analyze(List<Task> tasks, List<Task> previousPeriodTasks);
}
