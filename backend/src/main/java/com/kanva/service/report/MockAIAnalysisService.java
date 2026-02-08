package com.kanva.service.report;

import com.kanva.domain.task.Task;
import com.kanva.domain.task.TaskStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI 분석 서비스 Mock 구현
 * Gemini API 미사용 시 또는 테스트용
 */
@Service("mockAIAnalysisService")
public class MockAIAnalysisService implements AIAnalysisService {

    @Override
    public AnalysisResult analyze(AnalysisContext context) {
        List<Task> tasks = context.getCurrentTasks();
        List<Task> previousPeriodTasks = context.getPreviousPeriodTasks();

        int totalTasks = tasks.size();
        int completedTasks = (int) tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
                .count();

        int completionRate = totalTasks > 0
                ? (int) Math.round((double) completedTasks / totalTasks * 100)
                : 0;

        String trend = calculateTrend(tasks, previousPeriodTasks);
        boolean isStrict = "STRICT".equals(context.getTone());
        String summary = generateSummary(totalTasks, completedTasks, completionRate, trend, isStrict);
        String insights = generateInsights(tasks);
        String recommendations = generateRecommendations(tasks, completionRate, isStrict);

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

    private String generateSummary(int totalTasks, int completedTasks, int completionRate, String trend, boolean isStrict) {
        if (totalTasks == 0) {
            return isStrict
                    ? "이번 기간에 등록된 할 일이 없습니다. 할 일을 등록하지 않는 것 자체가 문제입니다."
                    : "이번 기간에 등록된 할 일이 없습니다. 새로운 목표를 설정해보세요.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("이번 기간 동안 총 %d개의 할 일 중 %d개를 완료했습니다. ",
                totalTasks, completedTasks));
        sb.append(String.format("완료율은 %d%%입니다. ", completionRate));

        if (isStrict) {
            switch (trend) {
                case "UP" -> sb.append("향상되긴 했지만 아직 갈 길이 멉니다.");
                case "DOWN" -> sb.append("완료율이 떨어지고 있습니다. 변명 대신 행동이 필요합니다.");
                case "STABLE" -> sb.append("제자리걸음입니다. 현 상태에 안주하지 마세요.");
                case "NEW" -> sb.append("첫 리포트입니다. 지금부터 제대로 해봅시다.");
            }
        } else {
            switch (trend) {
                case "UP" -> sb.append("지난 기간 대비 생산성이 향상되었습니다! 잘하고 계세요!");
                case "DOWN" -> sb.append("지난 기간보다 완료율이 낮아졌지만 괜찮아요. 다시 시작하면 됩니다.");
                case "STABLE" -> sb.append("꾸준한 생산성을 유지하고 있습니다. 대단해요!");
                case "NEW" -> sb.append("첫 번째 리포트입니다. 앞으로의 변화를 함께 추적해봐요!");
            }
        }

        return sb.toString();
    }

    private String generateInsights(List<Task> tasks) {
        if (tasks.isEmpty()) {
            return "분석할 데이터가 충분하지 않습니다.";
        }

        StringBuilder sb = new StringBuilder();

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

    private String generateRecommendations(List<Task> tasks, int completionRate, boolean isStrict) {
        StringBuilder sb = new StringBuilder();

        if (isStrict) {
            if (completionRate < 30) {
                sb.append("• 완료율 30% 미만은 심각합니다. 할 일을 즉시 줄이고 핵심만 남기세요.\n");
                sb.append("• 매일 최소 1개는 반드시 완료하세요. 습관부터 만드세요.\n");
            } else if (completionRate < 60) {
                sb.append("• 아직 부족합니다. 오늘 당장 가장 중요한 것부터 끝내세요.\n");
                sb.append("• 산만한 시간을 차단하세요. 집중력이 성과를 만듭니다.\n");
            } else if (completionRate < 80) {
                sb.append("• 나쁘지 않지만 만족하기엔 이릅니다. 80% 이상을 목표로 하세요.\n");
                sb.append("• 미완료 Task 패턴을 분석하고 원인을 제거하세요.\n");
            } else {
                sb.append("• 현재 수준을 유지하되, 더 높은 목표를 잡으세요.\n");
                sb.append("• 편한 일만 하고 있진 않은지 점검해보세요.\n");
            }
        } else {
            if (completionRate < 30) {
                sb.append("• 할 일을 더 작은 단위로 나눠보면 완료하기 수월해져요.\n");
                sb.append("• 하루에 완료할 수 있는 현실적인 목표를 설정해보세요.\n");
            } else if (completionRate < 60) {
                sb.append("• 중요한 할 일에 우선순위를 부여해보세요.\n");
                sb.append("• 방해 요소를 최소화하는 시간대에 집중해보세요.\n");
            } else if (completionRate < 80) {
                sb.append("• 좋은 진행입니다! 조금만 더 노력하면 목표를 달성할 수 있어요.\n");
                sb.append("• 완료된 할 일을 리뷰하고 패턴을 파악해보세요.\n");
            } else {
                sb.append("• 훌륭한 성과입니다! 현재 방식을 유지하세요.\n");
                sb.append("• 더 도전적인 목표를 설정해볼 수도 있습니다.\n");
            }
        }

        long overdueCount = tasks.stream().filter(Task::isOverdue).count();
        if (overdueCount > 0) {
            sb.append(isStrict
                    ? String.format("• 지연된 할 일이 %d개입니다. 즉시 처리하거나 포기 선언하세요.\n", overdueCount)
                    : "• 지연된 할 일을 검토하고 마감일을 재조정해보세요.\n");
        }

        return sb.toString().trim();
    }
}
