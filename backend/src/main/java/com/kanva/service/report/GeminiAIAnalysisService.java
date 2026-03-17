package com.kanva.service.report;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanva.domain.dailynote.DailyNote;
import com.kanva.domain.report.ReportPeriodType;
import com.kanva.domain.task.Task;
import com.kanva.domain.task.TaskStatus;
import com.kanva.service.gemini.GeminiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
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
    private static final DateTimeFormatter NATURAL_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("M월 d일(E)", Locale.KOREAN);

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
            String response = geminiClient.generateJsonContent(prompt,buildSchema());
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

        // 역할과 문체 설정
        boolean isStrict = "STRICT".equals(context.getTone());
        if (isStrict) {
            prompt.append("당신은 사용자의 생산성을 냉정하게 진단하는 시니어 코치입니다.\n");
            prompt.append("문체 규칙:\n");
            prompt.append("- 반말 존댓말 섞지 말고, 일관되게 '~입니다/~하세요' 체를 사용하세요\n");
            prompt.append("- 잘한 건 간결하게 인정하되, 못한 부분을 더 비중 있게 짚으세요\n");
            prompt.append("- '솔직히 말해서', '아쉽게도' 같은 직설적 연결어를 쓰세요\n");
            prompt.append("- 감정적 위로 없이 개선 방향을 명확히 제시하세요\n\n");
        } else {
            prompt.append("당신은 사용자와 매주 커피 한잔 하며 이야기하는 친근한 코칭 파트너입니다.\n");
            prompt.append("문체 규칙:\n");
            prompt.append("- 친구에게 말하듯 자연스럽고 따뜻하게 쓰세요 ('~했네요', '~거든요', '~어때요?')\n");
            prompt.append("- 딱딱한 보고서가 아니라, 대화하듯 써주세요\n");
            prompt.append("- 구체적 성과를 짚어 칭찬하되, 개선점은 '~해보는 건 어때요?' 형식으로 부드럽게\n");
            prompt.append("- 숫자를 나열하지 말고, 의미를 해석해서 이야기해주세요\n\n");
        }

        prompt.append("아래 데이터를 바탕으로, 사람이 직접 쓴 것처럼 자연스러운 피드백을 JSON으로 작성해주세요.\n\n");

        // 기간 정보
        String periodLabel = context.getPeriodType() == ReportPeriodType.WEEKLY ? "주간" : "월간";
        prompt.append(String.format("## 분석 기간: %s 리포트\n", periodLabel));

        // 전체 통계
        prompt.append("\n## 통계\n");
        prompt.append(String.format("- 전체: %d개 / 완료: %d개 / 완료율: %d%%\n", totalTasks, completedTasks, completionRate));
        if ("NEW".equals(trend)) {
            prompt.append("- 이전 대비: 없음 (첫 번째 리포트 — 비교할 이전 기간 데이터가 전혀 없습니다)\n");
        } else {
            prompt.append(String.format("- 이전 대비: %s\n", trend));
        }

        // 일자별 데이터
        List<DailyNote> dailyNotes = context.getDailyNotes();
        List<Task> tasks = context.getCurrentTasks();

        Map<LocalDate, List<Task>> tasksByDate = tasks.stream()
                .collect(Collectors.groupingBy(t -> {
                    if (t.getTaskDate() != null) return t.getTaskDate();
                    if (t.getDailyNote() != null && t.getDailyNote().getDate() != null) return t.getDailyNote().getDate();
                    return LocalDate.now();
                }));

        Map<LocalDate, DailyNote> notesByDate = dailyNotes.stream()
                .collect(Collectors.toMap(DailyNote::getDate, n -> n, (a, b) -> a));

        // 월간 리포트 - 주간 요약
        if (context.getPeriodType() == ReportPeriodType.MONTHLY && !tasks.isEmpty()) {
            prompt.append("\n## 주차별 요약\n");
            LocalDate earliest = tasksByDate.keySet().stream().min(LocalDate::compareTo).orElse(LocalDate.now());
            LocalDate latest = tasksByDate.keySet().stream().max(LocalDate::compareTo).orElse(LocalDate.now());

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

                prompt.append(String.format("- %d주차 (%s~%s): %d개 중 %d개 완료 (%d%%)\n",
                        weekNum, weekStart.format(NATURAL_DATE_FORMATTER), weekEnd.format(NATURAL_DATE_FORMATTER),
                        weekTotal, weekCompleted, weekRate));

                weekStart = weekEnd.plusDays(1);
                weekNum++;
            }
        }

        // 일자별 상세
        prompt.append("\n## 일자별 데이터\n");
        tasksByDate.keySet().stream().sorted().forEach(date -> {
            prompt.append(String.format("\n[%s]\n", date.format(NATURAL_DATE_FORMATTER)));

            DailyNote note = notesByDate.get(date);
            if (note != null && note.getContent() != null && !note.getContent().isBlank()) {
                String content = note.getContent();
                if (content.length() > 500) content = content.substring(0, 500) + "...";
                prompt.append(String.format("노트: %s\n", content));
            }

            List<Task> dateTasks = tasksByDate.get(date);
            if (dateTasks != null && !dateTasks.isEmpty()) {
                for (Task task : dateTasks) {
                    String statusIcon = task.getStatus() == TaskStatus.COMPLETED ? "✅"
                            : task.getStatus() == TaskStatus.IN_PROGRESS ? "🔄" : "⬜";
                    prompt.append(String.format("  %s %s", statusIcon, task.getTitle()));
                    if (task.getDescription() != null && !task.getDescription().isBlank()) {
                        String desc = task.getDescription();
                        if (desc.length() > 200) desc = desc.substring(0, 200) + "...";
                        prompt.append(String.format(" (%s)", desc));
                    }
                    if (task.isSeriesTask()) prompt.append(" [매일반복]");
                    if (task.isOverdue()) prompt.append(" [기한초과]");
                    prompt.append("\n");
                }
            }
        });

        // 노트만 있는 날
        notesByDate.keySet().stream()
                .filter(date -> !tasksByDate.containsKey(date))
                .sorted()
                .forEach(date -> {
                    DailyNote note = notesByDate.get(date);
                    if (note.getContent() != null && !note.getContent().isBlank()) {
                        prompt.append(String.format("\n[%s]\n", date.format(NATURAL_DATE_FORMATTER)));
                        String content = note.getContent();
                        if (content.length() > 500) content = content.substring(0, 500) + "...";
                        prompt.append(String.format("노트: %s\n(등록된 할 일 없음)\n", content));
                    }
                });

        // 이전 기간 통계
        List<Task> previousTasks = context.getPreviousPeriodTasks();
        if (previousTasks != null && !previousTasks.isEmpty()) {
            int prevTotal = previousTasks.size();
            int prevCompleted = (int) previousTasks.stream()
                    .filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
            int prevRate = prevTotal > 0 ? (int) Math.round((double) prevCompleted / prevTotal * 100) : 0;
            prompt.append(String.format("\n## 이전 기간: %d개 중 %d개 완료 (%d%%)\n", prevTotal, prevCompleted, prevRate));
        }

        // 응답 형식
        prompt.append("\n## 작성 규칙\n");
        prompt.append("3개 필드(summary, insights, recommendations)를 가진 JSON을 반환하세요.\n\n");

        prompt.append("summary (3~5문장):\n");
        prompt.append("- 이번 기간을 한마디로 정리하는 느낌으로 시작하세요\n");
        prompt.append("- 실제 할 일 이름을 자연스럽게 녹여 언급하세요\n");
        prompt.append("- 노트 내용이 있다면 반영하세요\n\n");

        prompt.append("insights (3~5개, 줄바꿈 구분, '• '로 시작):\n");
        prompt.append("- 숫자 나열이 아니라, 패턴이나 의미를 해석해주세요\n");
        prompt.append("- 요일별 흐름, 반복 항목 달성률, 카테고리별 완료율 등 관찰 가능한 사실 중심\n");
        prompt.append("- 노트에 직접 적힌 내용만 언급 가능 (감정/의도를 추측하지 말 것)\n\n");

        prompt.append("recommendations (3~5개, 줄바꿈 구분, '• '로 시작):\n");
        prompt.append("- 이 사용자의 실제 데이터에 기반한 구체적 제안만\n");
        prompt.append("- '일찍 일어나세요' 같은 일반론 금지\n\n");

        prompt.append("## 절대 금지\n");
        prompt.append("- 'Task'라는 영어 단어 사용 금지. 대신 자연스러운 표현(할 일, 항목, 목표 등)을 쓰세요\n");
        prompt.append("- '2026-02-08' 같은 ISO 날짜 포맷 금지. '2월 8일', '월요일', '이번 주 초' 같은 자연어를 쓰세요\n");
        prompt.append("- 데이터를 기계적으로 나열하지 마세요. 해석하고 의미를 붙여주세요\n");
        prompt.append("- 프롬프트의 구조를 그대로 반복하지 마세요. 자기 말로 풀어쓰세요\n");
        prompt.append("- summary에 모든 내용 몰아넣기 금지\n");
        prompt.append("- 빈 문자열 반환 금지\n");
        prompt.append("- Task 상태(진행 중, 미완료 등)에서 사용자의 심리, 의도, 자기 인식을 추측하지 마세요. '진행 중'은 단순히 시작했다는 의미일 뿐입니다.\n");
        prompt.append("- 데이터에 명시적으로 드러나지 않는 내용을 추론하지 마세요. 관찰 가능한 사실(완료율, 패턴, 빈도, 날짜별 분포)에만 기반하세요.\n");
        prompt.append("- [중요] '이전 대비'가 '없음'인 경우 이것은 사용자의 첫 번째 리포트입니다. ");
        prompt.append("이전 주/이전 기간과 비교하는 문장을 절대 만들지 마세요. ");
        prompt.append("'지난주보다', '이전보다', '전주 대비' 같은 비교 표현을 사용하지 마세요. ");
        prompt.append("대신 이번 기간 자체의 성과에만 집중하세요.\n\n");

        // few-shot (자연스러운 대화체)
        prompt.append("## 출력 예시 (이 톤과 자연스러움을 참고하세요)\n");
        prompt.append("{\n");
        prompt.append("  \"summary\": \"이번 주는 꽤 알찬 한 주였네요! 13개 중 9개를 해내서 완료율 69%를 기록했어요. ");
        prompt.append("특히 알고리즘 문제를 매일 빠짐없이 푼 게 눈에 띄어요. 주 후반에 이력서 작성과 포트폴리오 정리를 동시에 진행하느라 바빴을 텐데, ");
        prompt.append("노트에 적은 것처럼 체력적으로 힘들었을 수 있겠어요. 그래도 꾸준히 기록하면서 해내고 있는 모습이 대단합니다.\",\n");
        prompt.append("  \"insights\": \"• 알고리즘 풀이가 이번 주도 100% 달성이에요. 이 루틴은 확실히 습관으로 자리잡았네요\\n");
        prompt.append("• 주 초반(월~수)에 완료가 집중되고, 목금은 새로 등록만 하고 마무리 못한 항목이 많아요\\n");
        prompt.append("• 노트에서 '피곤하다'는 표현이 두 번 나왔어요. 후반부 생산성 저하와 연관이 있어 보여요\\n");
        prompt.append("• 이력서와 포트폴리오처럼 큰 작업은 하루에 몰아서 하려다가 미완료로 남는 패턴이 보여요\",\n");
        prompt.append("  \"recommendations\": \"• 이력서 같은 큰 작업은 '초안 쓰기→수정→최종본' 식으로 3일에 나눠보는 건 어때요?\\n");
        prompt.append("• 목금에 에너지가 떨어지니까 중요한 건 화수에 배치하면 완료율이 올라갈 거예요\\n");
        prompt.append("• 노트에 그날 컨디션을 한 줄이라도 적으면, 나중에 패턴 파악할 때 도움이 돼요\\n");
        prompt.append("• 매일 반복 항목이 잘 되고 있으니 거기에 '10분 스트레칭' 하나 추가해보는 것도 좋겠어요\"\n");
        prompt.append("}\n");

        return prompt.toString();
    }

    private AnalysisResult parseGeminiResponse(String response, int totalTasks, int completedTasks,
                                                int completionRate, String trend) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);

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

    private Map<String, Object> buildSchema() {

        // responseSchema로 JSON 구조 강제 (summary, insights, recommendations 필수)
        Map<String, Object> responseSchema = new java.util.HashMap<>();
        responseSchema.put("type", "OBJECT");
        responseSchema.put("properties", Map.of(
                "summary", Map.of("type", "STRING"),
                "insights", Map.of("type", "STRING"),
                "recommendations", Map.of("type", "STRING")
        ));
        responseSchema.put("required", List.of("summary", "insights", "recommendations"));

        return responseSchema;
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

}
