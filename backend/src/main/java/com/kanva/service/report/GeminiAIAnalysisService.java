package com.kanva.service.report;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanva.domain.task.Task;
import com.kanva.domain.task.TaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
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
    public AnalysisResult analyze(List<Task> tasks, List<Task> previousPeriodTasks) {
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
            return fallbackService.analyze(tasks, previousPeriodTasks);
        }

        try {
            String prompt = buildAnalysisPrompt(tasks, previousPeriodTasks, totalTasks, completedTasks, completionRate, trend);
            String response = geminiClient.generateContent(prompt);

            return parseGeminiResponse(response, totalTasks, completedTasks, completionRate, trend);
        } catch (Exception e) {
            log.error("Gemini analysis failed, using fallback: {}", e.getMessage());
            return fallbackService.analyze(tasks, previousPeriodTasks);
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

    private String buildAnalysisPrompt(List<Task> tasks, List<Task> previousTasks,
                                       int totalTasks, int completedTasks, int completionRate, String trend) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 개인 생산성 분석 AI 어시스턴트입니다. 아래 데이터를 분석하여 JSON 형식으로 결과를 제공해주세요.\n\n");

        // 현재 기간 데이터
        prompt.append("## 현재 기간 통계\n");
        prompt.append(String.format("- 총 할 일: %d개\n", totalTasks));
        prompt.append(String.format("- 완료: %d개\n", completedTasks));
        prompt.append(String.format("- 완료율: %d%%\n", completionRate));
        prompt.append(String.format("- 추세: %s\n", trend));

        // Task 세부 정보
        if (!tasks.isEmpty()) {
            prompt.append("\n## 현재 기간 할 일 목록\n");
            prompt.append(formatTaskList(tasks));
        }

        // 이전 기간 데이터
        if (previousTasks != null && !previousTasks.isEmpty()) {
            int prevTotal = previousTasks.size();
            int prevCompleted = (int) previousTasks.stream()
                    .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
                    .count();
            int prevRate = prevTotal > 0 ? (int) Math.round((double) prevCompleted / prevTotal * 100) : 0;

            prompt.append("\n## 이전 기간 통계 (비교용)\n");
            prompt.append(String.format("- 총 할 일: %d개\n", prevTotal));
            prompt.append(String.format("- 완료: %d개\n", prevCompleted));
            prompt.append(String.format("- 완료율: %d%%\n", prevRate));
        }

        // 응답 형식 지정
        prompt.append("\n## 응답 형식\n");
        prompt.append("반드시 아래 JSON 형식으로만 응답해주세요. 다른 텍스트 없이 JSON만 출력하세요:\n");
        prompt.append("```json\n");
        prompt.append("{\n");
        prompt.append("  \"summary\": \"전체 분석 요약 (2-3문장, 한국어)\",\n");
        prompt.append("  \"insights\": \"주요 인사이트 (불릿포인트 형식, 각 줄 '• '로 시작, 한국어)\",\n");
        prompt.append("  \"recommendations\": \"구체적인 개선 권장사항 (불릿포인트 형식, 각 줄 '• '로 시작, 한국어)\"\n");
        prompt.append("}\n");
        prompt.append("```\n");

        return prompt.toString();
    }

    private String formatTaskList(List<Task> tasks) {
        return tasks.stream()
                .map(task -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append(String.format("- [%s] %s", task.getStatus().name(), task.getTitle()));
                    if (task.getTaskDate() != null) {
                        sb.append(String.format(" (날짜: %s)", task.getTaskDate().format(DATE_FORMATTER)));
                    }
                    if (task.getDueDate() != null) {
                        sb.append(String.format(" (마감: %s)", task.getDueDate().format(DATE_FORMATTER)));
                        if (task.isOverdue()) {
                            sb.append(" [지연]");
                        }
                    }
                    return sb.toString();
                })
                .collect(Collectors.joining("\n"));
    }

    private AnalysisResult parseGeminiResponse(String response, int totalTasks, int completedTasks,
                                                int completionRate, String trend) {
        try {
            // JSON 블록 추출 (```json ... ``` 형식 처리)
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
        // ```json ... ``` 블록에서 JSON 추출
        if (response.contains("```json")) {
            int start = response.indexOf("```json") + 7;
            int end = response.indexOf("```", start);
            if (end > start) {
                return response.substring(start, end).trim();
            }
        }
        // ``` ... ``` 블록에서 JSON 추출
        if (response.contains("```")) {
            int start = response.indexOf("```") + 3;
            int end = response.indexOf("```", start);
            if (end > start) {
                return response.substring(start, end).trim();
            }
        }
        // JSON 객체 직접 찾기
        if (response.contains("{")) {
            int start = response.indexOf("{");
            int end = response.lastIndexOf("}") + 1;
            if (end > start) {
                return response.substring(start, end);
            }
        }
        return response.trim();
    }
}
