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
        String trend = calculateTrend(tasks, previousPeriodTasks);

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

    private String buildAnalysisPrompt(AnalysisContext context, int totalTasks,
                                       int completedTasks, int completionRate, String trend) {
        StringBuilder prompt = new StringBuilder();

        // 톤 설정
        boolean isStrict = "STRICT".equals(context.getTone());
        if (isStrict) {
            prompt.append("당신은 직설적이고 엄격한 생산성 코치입니다. ");
            prompt.append("칭찬보다는 냉정한 현실 직시를 돕고, 변명 없이 행동을 촉구하는 스타일입니다. ");
            prompt.append("단, 인격 모독이 아니라 성장을 위한 채찍질이어야 합니다.\n\n");
        } else {
            prompt.append("당신은 따뜻하고 격려하는 생산성 코치입니다. ");
            prompt.append("사용자의 노력을 인정하고, 작은 성과도 칭찬하며, 개선점은 부드럽게 제안합니다. ");
            prompt.append("사용자가 동기부여를 받을 수 있도록 응원해주세요.\n\n");
        }

        prompt.append("아래는 사용자의 생산성 데이터입니다. ");
        prompt.append("데일리 노트(일기/메모)와 할 일(Task) 데이터를 종합적으로 분석하여, ");
        prompt.append("이 사용자가 어떤 사람인지 이해하고 맞춤형 피드백을 제공해주세요.\n\n");

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

        // 일자별 그룹핑
        Map<LocalDate, List<Task>> tasksByDate = tasks.stream()
                .filter(t -> t.getTaskDate() != null)
                .collect(Collectors.groupingBy(Task::getTaskDate));

        Map<LocalDate, DailyNote> notesByDate = dailyNotes.stream()
                .collect(Collectors.toMap(DailyNote::getDate, n -> n, (a, b) -> a));

        // 월간 리포트일 때 주 단위 그룹핑
        if (context.getPeriodType() == ReportPeriodType.MONTHLY && !tasks.isEmpty()) {
            prompt.append("\n## 주간별 통계\n");
            LocalDate earliest = tasks.stream()
                    .map(Task::getTaskDate)
                    .filter(d -> d != null)
                    .min(LocalDate::compareTo)
                    .orElse(LocalDate.now());
            LocalDate latest = tasks.stream()
                    .map(Task::getTaskDate)
                    .filter(d -> d != null)
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
                        .filter(t -> t.getTaskDate() != null
                                && !t.getTaskDate().isBefore(ws)
                                && !t.getTaskDate().isAfter(we))
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

        // 분석 가이드라인
        prompt.append("\n## 분석 가이드라인\n");
        prompt.append("- 데일리 노트 내용을 읽고 사용자의 관심사, 고민, 감정 상태를 파악하세요\n");
        prompt.append("- Task의 제목과 설명에서 사용자가 어떤 분야에서 활동하는지 이해하세요\n");
        prompt.append("- 반복 Task의 완료 패턴에서 습관 형성 여부를 분석하세요\n");
        prompt.append("- 지연된 Task가 있다면 그 원인을 데일리 노트와 연결지어 분석하세요\n");
        prompt.append("- 숫자 나열이 아닌, 이 사용자에게 실질적으로 도움이 되는 맞춤형 조언을 하세요\n\n");

        // 응답 형식
        prompt.append("## 응답 형식\n");
        prompt.append("반드시 아래 JSON 형식으로만 응답해주세요. 다른 텍스트 없이 JSON만 출력하세요:\n");
        prompt.append("```json\n");
        prompt.append("{\n");
        prompt.append("  \"summary\": \"전체 분석 요약 (3-5문장, 한국어, 데일리 노트 내용을 반영한 맞춤형 분석)\",\n");
        prompt.append("  \"insights\": \"주요 인사이트 (불릿포인트 형식, 각 줄 '• '로 시작, 3-5개, 한국어)\",\n");
        prompt.append("  \"recommendations\": \"구체적인 개선 권장사항 (불릿포인트 형식, 각 줄 '• '로 시작, 3-5개, 한국어)\"\n");
        prompt.append("}\n");
        prompt.append("```\n");

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
