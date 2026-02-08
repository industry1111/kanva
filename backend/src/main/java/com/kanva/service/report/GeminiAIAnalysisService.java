package com.kanva.service.report;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanva.domain.dailynote.DailyNote;
import com.kanva.domain.report.ReportPeriodType;
import com.kanva.domain.task.Task;
import com.kanva.domain.task.TaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Gemini AI를 사용한 분석 서비스
 */
@Slf4j
@Service("geminiAIAnalysisService")
@RequiredArgsConstructor
public class GeminiAIAnalysisService implements AIAnalysisService {

    private final GeminiClient geminiClient;
    private final MockAIAnalysisService fallbackService;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public AnalysisResult analyze(AnalysisContext context) {
        List<Task> tasks = context.getCurrentTasks();
        List<Task> previousPeriodTasks = context.getPreviousPeriodTasks();

        // 기본 통계 계산
        int totalTasks = tasks.size();
        int completedTasks = (int) tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
                .count();
        int completionRate = totalTasks > 0
                ? (int) Math.round((double) completedTasks / totalTasks * 100)
                : 0;
        String trend = calculateTrend(tasks, previousPeriodTasks, context.getPreviousReportCompletionRate());

        // Gemini API 사용 불가 시 fallback
        if (!geminiClient.isAvailable()) {
            log.info("Gemini API not available, using fallback service");
            return fallbackService.analyze(context);
        }

        try {
            String prompt = buildAnalysisPrompt(context, totalTasks, completedTasks, completionRate, trend);
            String response = geminiClient.generateContent(prompt);
            log.debug("Gemini raw response: {}", response);

            return parseGeminiResponse(response, totalTasks, completedTasks, completionRate, trend);
        } catch (Exception e) {
            log.error("Gemini analysis failed, using fallback: {}", e.getMessage());
            return fallbackService.analyze(context);
        }
    }

    private String calculateTrend(List<Task> currentTasks, List<Task> previousTasks,
                                   Integer previousReportCompletionRate) {
        // 1. 이전 기간 Task 데이터가 있으면 직접 비교
        if (previousTasks != null && !previousTasks.isEmpty()) {
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

            return compareTrend(currentRate, prevRate);
        }

        // 2. 이전 리포트의 completionRate가 있으면 그걸로 비교
        if (previousReportCompletionRate != null) {
            int currentTotal = currentTasks.size();
            int currentCompleted = (int) currentTasks.stream()
                    .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
                    .count();
            int currentRate = currentTotal > 0 ? (int) Math.round((double) currentCompleted / currentTotal * 100) : 0;

            return compareTrend(currentRate, previousReportCompletionRate);
        }

        // 3. 비교 대상 없음
        return "NEW";
    }

    private String compareTrend(int currentRate, int prevRate) {
        int diff = currentRate - prevRate;
        if (diff > 5) {
            return "UP";
        } else if (diff < -5) {
            return "DOWN";
        } else {
            return "STABLE";
        }
    }

    private String buildAnalysisPrompt(AnalysisContext context, int totalTasks,
                                       int completedTasks, int completionRate, String trend) {
        StringBuilder prompt = new StringBuilder();

        // 톤 설정 (구체적 행동 지침)
        boolean isStrict = "STRICT".equals(context.getTone());
        if (isStrict) {
            prompt.append("당신은 냉철한 성과 관리자입니다.\n");
            prompt.append("규칙:\n");
            prompt.append("- 데이터에 기반한 팩트만 전달하세요\n");
            prompt.append("- 칭찬은 최소화하고, 미완료 Task와 비효율적 패턴을 날카롭게 지적하세요\n");
            prompt.append("- '~해야 합니다', '~하지 마세요' 형식의 직접적 명령어를 사용하세요\n");
            prompt.append("- 감정적 위로 없이 냉정하게 분석하세요\n\n");
        } else {
            prompt.append("당신은 사용자의 코칭 파트너입니다.\n");
            prompt.append("규칙:\n");
            prompt.append("- 구체적인 성과를 짚어서 칭찬하세요 (예: '매일 빠짐없이 알고리즘 문제를 푼 건 대단합니다')\n");
            prompt.append("- 개선점은 '~하면 더 좋을 것 같아요' 형식으로 부드럽게 제안하세요\n");
            prompt.append("- 사용자의 노력과 성장을 인정하는 따뜻한 톤을 유지하세요\n\n");
        }

        prompt.append("아래 사용자의 생산성 데이터를 분석하여 맞춤형 피드백을 JSON으로 제공하세요.\n\n");

        // 기간 정보
        prompt.append(String.format("## 분석 기간: %s 리포트\n",
                context.getPeriodType() == ReportPeriodType.WEEKLY ? "주간" : "월간"));

        // 전체 통계
        prompt.append("\n## 전체 통계\n");
        prompt.append(String.format("- 총 할 일: %d개\n", totalTasks));
        prompt.append(String.format("- 완료: %d개\n", completedTasks));
        prompt.append(String.format("- 완료율: %d%%\n", completionRate));
        prompt.append(String.format("- 이전 기간 대비 추세: %s\n", trend));

        // 일자별 데일리 노트 + Task
        List<DailyNote> dailyNotes = context.getDailyNotes();
        List<Task> tasks = context.getCurrentTasks();

        // 일자별 그룹핑 (taskDate가 null이면 dailyNote.date 사용)
        Map<LocalDate, List<Task>> tasksByDate = tasks.stream()
                .collect(Collectors.groupingBy(t -> {
                    if (t.getTaskDate() != null) return t.getTaskDate();
                    if (t.getDailyNote() != null && t.getDailyNote().getDate() != null) return t.getDailyNote().getDate();
                    return LocalDate.now(); // 최후 fallback
                }));

        Map<LocalDate, DailyNote> notesByDate = dailyNotes.stream()
                .collect(Collectors.toMap(DailyNote::getDate, n -> n, (a, b) -> a));

        // 월간 리포트일 때 주 단위 그룹핑
        if (context.getPeriodType() == ReportPeriodType.MONTHLY && !tasks.isEmpty()) {
            prompt.append("\n## 주간별 통계\n");
            LocalDate earliest = tasksByDate.keySet().stream()
                    .min(LocalDate::compareTo)
                    .orElse(LocalDate.now());
            LocalDate latest = tasksByDate.keySet().stream()
                    .max(LocalDate::compareTo)
                    .orElse(LocalDate.now());

            LocalDate weekStart = earliest;
            int weekNum = 1;
            while (!weekStart.isAfter(latest)) {
                LocalDate weekEnd = weekStart.plusDays(6);
                if (weekEnd.isAfter(latest)) weekEnd = latest;

                LocalDate ws = weekStart;
                LocalDate we = weekEnd;
                List<Task> weekTasks = tasks.stream()
                        .filter(t -> {
                            LocalDate d = t.getTaskDate() != null ? t.getTaskDate()
                                    : (t.getDailyNote() != null ? t.getDailyNote().getDate() : null);
                            return d != null && !d.isBefore(ws) && !d.isAfter(we);
                        })
                        .toList();

                int weekTotal = weekTasks.size();
                int weekCompleted = (int) weekTasks.stream()
                        .filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
                int weekRate = weekTotal > 0 ? (int) Math.round((double) weekCompleted / weekTotal * 100) : 0;

                prompt.append(String.format("- %d주차 (%s ~ %s): %d개 중 %d개 완료 (%d%%)\n",
                        weekNum, weekStart.format(DATE_FORMATTER), weekEnd.format(DATE_FORMATTER),
                        weekTotal, weekCompleted, weekRate));

                weekStart = weekEnd.plusDays(1);
                weekNum++;
            }
        }

        // 일자별 상세
        prompt.append("\n## 일자별 상세 데이터\n");
        tasksByDate.keySet().stream().sorted().forEach(date -> {
            prompt.append(String.format("\n### %s\n", date.format(DATE_FORMATTER)));

            // 데일리 노트
            DailyNote note = notesByDate.get(date);
            if (note != null && note.getContent() != null && !note.getContent().isBlank()) {
                String content = note.getContent();
                if (content.length() > 500) {
                    content = content.substring(0, 500) + "...";
                }
                prompt.append(String.format("**데일리 노트**: %s\n", content));
            }

            // Task 목록
            List<Task> dateTasks = tasksByDate.get(date);
            if (dateTasks != null && !dateTasks.isEmpty()) {
                prompt.append("**할 일**:\n");
                for (Task task : dateTasks) {
                    prompt.append(String.format("  - [%s] %s", task.getStatus().name(), task.getTitle()));
                    if (task.getDescription() != null && !task.getDescription().isBlank()) {
                        String desc = task.getDescription();
                        if (desc.length() > 200) desc = desc.substring(0, 200) + "...";
                        prompt.append(String.format(" - %s", desc));
                    }
                    if (task.isSeriesTask()) {
                        prompt.append(" [반복]");
                    }
                    if (task.isOverdue()) {
                        prompt.append(" [지연]");
                    }
                    prompt.append("\n");
                }
            }
        });

        // 노트만 있고 Task가 없는 날짜도 포함
        notesByDate.keySet().stream()
                .filter(date -> !tasksByDate.containsKey(date))
                .sorted()
                .forEach(date -> {
                    DailyNote note = notesByDate.get(date);
                    if (note.getContent() != null && !note.getContent().isBlank()) {
                        prompt.append(String.format("\n### %s\n", date.format(DATE_FORMATTER)));
                        String content = note.getContent();
                        if (content.length() > 500) content = content.substring(0, 500) + "...";
                        prompt.append(String.format("**데일리 노트**: %s\n", content));
                        prompt.append("**할 일**: 없음\n");
                    }
                });

        // 이전 기간 통계
        List<Task> previousTasks = context.getPreviousPeriodTasks();
        if (previousTasks != null && !previousTasks.isEmpty()) {
            int prevTotal = previousTasks.size();
            int prevCompleted = (int) previousTasks.stream()
                    .filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
            int prevRate = prevTotal > 0 ? (int) Math.round((double) prevCompleted / prevTotal * 100) : 0;

            prompt.append("\n## 이전 기간 통계 (비교용)\n");
            prompt.append(String.format("- 총 할 일: %d개, 완료: %d개, 완료율: %d%%\n", prevTotal, prevCompleted, prevRate));
        }

        // 응답 형식 (code fence 없이, 필드별 상세 요구사항)
        prompt.append("\n## 응답 규칙\n");
        prompt.append("아래 3개 필드를 가진 JSON 객체를 반환하세요. 모든 필드는 반드시 내용을 채워야 합니다.\n\n");

        prompt.append("**summary** (필수, 3~5문장):\n");
        prompt.append("- 반드시 실제 Task 제목을 1개 이상 언급하세요 (예: '알고리즘 1day 1commit')\n");
        prompt.append("- 데일리 노트에 적힌 내용이 있다면 반드시 반영하세요\n");
        prompt.append("- 완료율 수치와 그 의미를 해석하세요\n\n");

        prompt.append("**insights** (필수, 불릿포인트 3~5개):\n");
        prompt.append("- 각 불릿은 줄바꿈(\\n)으로 구분하고, '• '로 시작하세요\n");
        prompt.append("- 각 인사이트마다 구체적 데이터 근거를 포함하세요 (Task명, 날짜, 완료율 등)\n");
        prompt.append("- 요일별/시간별 패턴, 반복 Task 완료 패턴, 노트에서 발견한 감정/관심사를 분석하세요\n");
        prompt.append("- 통계 섹션과 동일한 숫자만 반복하지 마세요\n\n");

        prompt.append("**recommendations** (필수, 불릿포인트 3~5개):\n");
        prompt.append("- 각 불릿은 줄바꿈(\\n)으로 구분하고, '• '로 시작하세요\n");
        prompt.append("- 동사로 시작하는 실행 가능한 조언만 적으세요\n");
        prompt.append("- 이 사용자의 실제 Task와 노트 데이터에 기반한 맞춤 조언이어야 합니다\n");
        prompt.append("- '일찍 일어나세요' 같은 일반적 생산성 팁은 금지합니다\n\n");

        prompt.append("**금지사항**:\n");
        prompt.append("- '전체적으로 잘하고 계십니다' 같은 모호한 평가 금지\n");
        prompt.append("- 빈 문자열(\"\") 반환 금지. 모든 필드에 반드시 내용을 채우세요\n");
        prompt.append("- summary에 모든 내용을 몰아넣지 마세요. insights와 recommendations에 각각 다른 내용을 작성하세요\n\n");

        // few-shot 예시
        prompt.append("## 출력 예시\n");
        prompt.append("{\n");
        prompt.append("  \"summary\": \"이번 주에 총 12개의 할 일 중 9개를 완료하여 75%의 완료율을 기록했습니다. ");
        prompt.append("특히 '알고리즘 1day 1commit'을 매일 빠짐없이 수행한 점이 인상적입니다. ");
        prompt.append("데일리 노트에서 '면접 준비가 걱정된다'고 적었는데, 실제로 이력서 관련 Task를 3개 완료하며 행동으로 옮기고 있습니다. ");
        prompt.append("다만 '운동하기' Task가 3일 연속 미완료 상태입니다.\",\n");
        prompt.append("  \"insights\": \"• '알고리즘 1day 1commit' 반복 Task의 완료율이 100%로, 코딩 습관이 완전히 자리잡았습니다\\n");
        prompt.append("• 화요일과 수요일에 Task 완료가 집중되어 있고, 목금은 완료율이 낮습니다\\n");
        prompt.append("• 데일리 노트에 '피곤하다'는 표현이 3회 등장하여 체력 관리가 생산성에 영향을 주고 있습니다\\n");
        prompt.append("• '운동하기' Task는 등록만 하고 한 번도 완료하지 않아 목표 재설정이 필요합니다\",\n");
        prompt.append("  \"recommendations\": \"• 목금의 에너지 저하 패턴을 고려해 중요한 Task는 화수에 배치하세요\\n");
        prompt.append("• '운동하기'를 '10분 스트레칭'으로 목표를 축소하면 완료 확률이 높아집니다\\n");
        prompt.append("• 면접 준비 Task를 시리즈(반복)로 만들어 매일 30분씩 진행해보세요\\n");
        prompt.append("• 노트에 '오늘 잘한 일 1가지' 섹션을 추가하면 동기부여에 도움됩니다\"\n");
        prompt.append("}\n");

        return prompt.toString();
    }

    private AnalysisResult parseGeminiResponse(String response, int totalTasks, int completedTasks,
                                                int completionRate, String trend) {
        try {
            String jsonContent = extractJsonContent(response);
            JsonNode rootNode = objectMapper.readTree(jsonContent);

            String summary = rootNode.has("summary") ? rootNode.get("summary").asText() : "";
            String insights = rootNode.has("insights") ? rootNode.get("insights").asText() : "";
            String recommendations = rootNode.has("recommendations") ? rootNode.get("recommendations").asText() : "";

            // fallback: Gemini가 빈값을 반환한 경우 기본 내용 생성
            if (summary == null || summary.isBlank()) {
                log.warn("Gemini returned empty summary, generating fallback");
                summary = generateFallbackSummary(totalTasks, completedTasks, completionRate);
            }
            if (insights == null || insights.isBlank()) {
                log.warn("Gemini returned empty insights, generating fallback");
                insights = generateFallbackInsights(totalTasks, completedTasks, completionRate);
            }
            if (recommendations == null || recommendations.isBlank()) {
                log.warn("Gemini returned empty recommendations, generating fallback");
                recommendations = generateFallbackRecommendations(completionRate);
            }

            return AnalysisResult.builder()
                    .totalTasks(totalTasks)
                    .completedTasks(completedTasks)
                    .completionRate(completionRate)
                    .trend(trend)
                    .summary(summary)
                    .insights(insights)
                    .recommendations(recommendations)
                    .build();

        } catch (JsonProcessingException e) {
            log.error("Failed to parse Gemini response: {}", e.getMessage());
            throw new RuntimeException("Failed to parse Gemini response", e);
        }
    }

    private String generateFallbackSummary(int totalTasks, int completedTasks, int completionRate) {
        if (totalTasks == 0) {
            return "이번 기간에 등록된 할 일이 없습니다. 새로운 목표를 설정해보세요.";
        }
        return String.format("이번 기간 동안 총 %d개의 할 일 중 %d개를 완료하여 %d%%의 완료율을 기록했습니다.",
                totalTasks, completedTasks, completionRate);
    }

    private String generateFallbackInsights(int totalTasks, int completedTasks, int completionRate) {
        StringBuilder sb = new StringBuilder();
        int pending = totalTasks - completedTasks;
        if (completedTasks > 0) {
            sb.append(String.format("• 총 %d개의 할 일을 완료했습니다\n", completedTasks));
        }
        if (pending > 0) {
            sb.append(String.format("• 미완료 할 일이 %d개 남아있습니다\n", pending));
        }
        if (completionRate >= 80) {
            sb.append("• 높은 완료율을 유지하고 있습니다");
        } else if (completionRate >= 50) {
            sb.append("• 절반 이상의 할 일을 완료했습니다");
        } else if (totalTasks > 0) {
            sb.append("• 완료율 개선이 필요합니다");
        }
        return sb.toString().trim();
    }

    private String generateFallbackRecommendations(int completionRate) {
        StringBuilder sb = new StringBuilder();
        if (completionRate < 50) {
            sb.append("• 할 일을 더 작은 단위로 나눠서 완료하기 쉽게 만들어보세요\n");
            sb.append("• 하루에 완료할 수 있는 현실적인 목표를 설정해보세요\n");
            sb.append("• 가장 중요한 할 일에 우선순위를 부여해보세요");
        } else if (completionRate < 80) {
            sb.append("• 미완료된 할 일의 패턴을 분석해보세요\n");
            sb.append("• 방해 요소를 최소화하는 시간대에 집중해보세요\n");
            sb.append("• 반복 Task를 활용하여 습관을 만들어보세요");
        } else {
            sb.append("• 현재의 좋은 습관을 유지하세요\n");
            sb.append("• 더 도전적인 목표를 설정해볼 수 있습니다\n");
            sb.append("• 완료된 할 일을 리뷰하고 개선점을 찾아보세요");
        }
        return sb.toString().trim();
    }

    private String extractJsonContent(String response) {
        // Gemini 2.5 Flash의 thinking 블록 제거
        String cleaned = response.replaceAll("(?s)<think>.*?</think>", "").trim();

        if (cleaned.contains("```json")) {
            int start = cleaned.indexOf("```json") + 7;
            int end = cleaned.indexOf("```", start);
            if (end > start) {
                return cleaned.substring(start, end).trim();
            }
        }
        if (cleaned.contains("```")) {
            int start = cleaned.indexOf("```") + 3;
            int end = cleaned.indexOf("```", start);
            if (end > start) {
                return cleaned.substring(start, end).trim();
            }
        }
        if (cleaned.contains("{")) {
            int start = cleaned.indexOf("{");
            int end = cleaned.lastIndexOf("}") + 1;
            if (end > start) {
                return cleaned.substring(start, end);
            }
        }
        return cleaned.trim();
    }
}
