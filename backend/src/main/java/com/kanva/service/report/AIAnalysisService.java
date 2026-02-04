package com.kanva.service.report;

import com.kanva.domain.task.Task;
import com.kanva.domain.task.TaskStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI 분석 서비스 (Mock 구현)
 * 향후 실제 AI API 연동 예정
 */
@Service
@RequiredArgsConstructor
public class AIAnalysisService {

    @Getter
    @Builder
    public static class AnalysisResult {
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
     */
    public AnalysisResult analyze(List<Task> tasks, List<Task> previousPeriodTasks) {
        int totalTasks = tasks.size();
        int completedTasks = (int) tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
                .count();

        int completionRate = totalTasks > 0
                ? (int) Math.round((double) completedTasks / totalTasks * 100)
                : 0;

        String trend = calculateTrend(tasks, previousPeriodTasks);
        String summary = generateSummary(totalTasks, completedTasks, completionRate, trend);
        String insights = generateInsights(tasks);
        String recommendations = generateRecommendations(tasks, completionRate);

        return AnalysisResult.builder()
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .completionRate(completionRate)
                .trend(trend)
                .summary(summary)
                .insights(insights)
                .recommendations(recommendations)
                .build();
    }

    private String calculateTrend(List<Task> currentTasks, List<Task> previousTasks) {
        if (previousTasks == null || previousTasks.isEmpty()) {
            return "NEW";
        }

        int currentTotal = currentTasks.size();
        int currentCompleted = (int) currentTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
                .count();
        int currentRate = currentTotal > 0 ? (int) Math.round((double) currentCompleted / currentTotal * 100) : 0;

        int prevTotal = previousTasks.size();
        int prevCompleted = (int) previousTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
                .count();
        int prevRate = prevTotal > 0 ? (int) Math.round((double) prevCompleted / prevTotal * 100) : 0;

        int diff = currentRate - prevRate;
        if (diff > 5) {
            return "UP";
        } else if (diff < -5) {
            return "DOWN";
        } else {
            return "STABLE";
        }
    }

    private String generateSummary(int totalTasks, int completedTasks, int completionRate, String trend) {
        if (totalTasks == 0) {
            return "이번 기간에 등록된 할 일이 없습니다. 새로운 목표를 설정해보세요.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("이번 기간 동안 총 %d개의 할 일 중 %d개를 완료했습니다. ",
                totalTasks, completedTasks));
        sb.append(String.format("완료율은 %d%%입니다. ", completionRate));

        switch (trend) {
            case "UP" -> sb.append("지난 기간 대비 생산성이 향상되었습니다!");
            case "DOWN" -> sb.append("지난 기간보다 완료율이 낮아졌습니다. 우선순위를 재검토해보세요.");
            case "STABLE" -> sb.append("꾸준한 생산성을 유지하고 있습니다.");
            case "NEW" -> sb.append("첫 번째 리포트입니다. 앞으로의 변화를 추적해보세요!");
        }

        return sb.toString();
    }

    private String generateInsights(List<Task> tasks) {
        if (tasks.isEmpty()) {
            return "분석할 데이터가 충분하지 않습니다.";
        }

        StringBuilder sb = new StringBuilder();

        // 미완료 Task 분석
        long pendingCount = tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.PENDING)
                .count();
        long inProgressCount = tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS)
                .count();

        if (pendingCount > 0) {
            sb.append(String.format("• 시작하지 않은 할 일이 %d개 있습니다.\n", pendingCount));
        }
        if (inProgressCount > 0) {
            sb.append(String.format("• 진행 중인 할 일이 %d개 있습니다.\n", inProgressCount));
        }

        // 지연된 Task 분석
        long overdueCount = tasks.stream()
                .filter(Task::isOverdue)
                .count();
        if (overdueCount > 0) {
            sb.append(String.format("• 마감일이 지난 할 일이 %d개 있습니다.\n", overdueCount));
        }

        if (sb.isEmpty()) {
            sb.append("• 모든 할 일을 완료했습니다! 훌륭합니다.");
        }

        return sb.toString().trim();
    }

    private String generateRecommendations(List<Task> tasks, int completionRate) {
        StringBuilder sb = new StringBuilder();

        if (completionRate < 30) {
            sb.append("• 할 일을 더 작은 단위로 나눠보세요.\n");
            sb.append("• 하루에 완료할 수 있는 현실적인 목표를 설정하세요.\n");
        } else if (completionRate < 60) {
            sb.append("• 중요한 할 일에 우선순위를 부여해보세요.\n");
            sb.append("• 방해 요소를 최소화하는 시간대에 집중해보세요.\n");
        } else if (completionRate < 80) {
            sb.append("• 좋은 진행입니다! 조금만 더 노력하면 목표를 달성할 수 있습니다.\n");
            sb.append("• 완료된 할 일을 리뷰하고 패턴을 파악해보세요.\n");
        } else {
            sb.append("• 훌륭한 성과입니다! 현재 방식을 유지하세요.\n");
            sb.append("• 더 도전적인 목표를 설정해볼 수도 있습니다.\n");
        }

        // 지연된 Task가 있으면 추가 추천
        long overdueCount = tasks.stream().filter(Task::isOverdue).count();
        if (overdueCount > 0) {
            sb.append("• 지연된 할 일을 검토하고 마감일을 재조정해보세요.\n");
        }

        return sb.toString().trim();
    }
}
