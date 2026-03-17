package com.kanva.service.parsing;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanva.domain.task.Task;
import com.kanva.service.gemini.GeminiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Gemini AI를 사용한 분석 서비스
 */
@Slf4j
@Service("geminiAIParsingService")
@RequiredArgsConstructor
public class GeminiAIParsingService implements AIParsingService{

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    public List<ParsingResult> parsing(ParsingContext context){
        log.info("=== parsing 시작 ===");
        LocalDate date = context.dailyNote.getDate();
        String content = context.dailyNote.getContent();

        List<Task> registeredTasks = context.registeredTasks;

        String prompt = buildParsingPrompt(date,content,registeredTasks);

        String response = geminiClient.generateJsonContent(prompt,buildSchema());

        return parseGeminiResponse(response,registeredTasks);
    }

    public String buildParsingPrompt(LocalDate date, String content,List<Task> taskList) {

        StringBuilder prompt = new StringBuilder();

        String tasksInfo = taskListToString(taskList);

        // 1. 역할 부여
        prompt.append("너는 텍스트에서 업무(WORK)와 일정(SCHEDULE)을 추출하는 AI야.\n\n");

        // 2. 기준 날짜
        prompt.append(String.format("기준 날짜: %s\n", date));
        prompt.append("\"내일\"은 기준 날짜 +1일, \"다음주 월요일\"은 기준 날짜 기준으로 계산해.\n\n");
        prompt.append("\"7월말\"은 해당 연도 7월 31일, \"월말\"은 해당 월 마지막 날로 변환해.\n\n");
        prompt.append("\"다음주\"는 기준 날짜가 속한 주의 다음 주를 의미해. 이번주와 혼동하지 마.\n");


        // 3. 입력 텍스트
        prompt.append("=== 입력 텍스트 ===\n");
        prompt.append(content).append("\n\n");

        // 4. 기존 등록된 태스크 (중복 방지)
        if (!tasksInfo.isEmpty()) {
            prompt.append("=== 이미 등록된 항목 (중복 추출 금지) ===\n");
            prompt.append(tasksInfo).append("\n");
        }

        // 5. 추출 규칙
        prompt.append("=== 추출 규칙 ===\n");
        prompt.append("type 판단:\n");
        prompt.append("- 특정 날짜/시간이 정해진 약속, 회의, 미팅 → SCHEDULE\n");
        prompt.append("- 해야 할 작업, 할 일 → WORK\n\n");

        prompt.append("category 판단:\n");
        prompt.append("- 업무, 회의, 보고서, 개발 등 → WORK\n");
        prompt.append("- 운동, 헬스, 러닝, 스트레칭 등 → EXERCISE\n");
        prompt.append("- 그 외 → OTHER\n\n");

        prompt.append("status 판단:\n");
        prompt.append("- \"완료\", \"했다\", \"끝냄\", \"다녀옴\" 등 완료 표현 → COMPLETED\n");
        prompt.append("- [x] 체크된 항목 → COMPLETED\n");
        prompt.append("- [ ] 체크 안 된 항목 → PENDING\n");
        prompt.append("- 그 외 → PENDING\n\n");

        prompt.append("항목 구분:\n");
        prompt.append("- 번호, 줄바꿈, 글머리 기호로 구분된 각 항목은 별도 업무로 추출해.\n");
        prompt.append("- 단, 하나의 업무에 대한 부연 설명은 description에 합쳐.\n\n");
        prompt.append("- 참고 사항, 원칙, 환경 설명 등 행동이 아닌 내용은 추출하지 마.\n");
        prompt.append("- \"=> \"로 시작하는 줄은 윗 항목의 부연 설명이므로 description에 합쳐.\n");

        prompt.append("계층형 업무일지 규칙:\n");
        prompt.append("- 번호 항목(1. 2. 3.) 아래 \"-\"로 시작하는 하위 항목이 있으면, 번호 항목은 프로젝트/고객명이고 하위 항목이 실제 업무야.\n");
        prompt.append("- 이 경우 title은 \"프로젝트명 + 하위 업무명\" 형태로 합쳐. (예: \"경남제약 LIMS 테스트 및 수정\")\n");
        prompt.append("- 퍼센트(100%, 10%)가 있으면: 100% → COMPLETED, 그 외 → PENDING\n");
        prompt.append("- \"기타사항 : 외근(날짜)\" → SCHEDULE로 추출, title은 \"프로젝트명 외근\", startDateTime에 해당 날짜를 넣어.\n");
        prompt.append("- \"기타사항 : X\" 또는 \"기타사항 : 없음\" → 추출하지 마.\n");
        prompt.append("- 번호 항목 자체(프로젝트명만 있는 줄)는 추출하지 마. 하위 항목만 추출해.\n\n");

        // 6. 금지 규칙
        prompt.append("=== 금지 규칙 ===\n");
        prompt.append("- 텍스트에 없는 내용을 추측하여 만들지 마.\n");
        prompt.append("- 하나의 항목을 여러 개로 쪼개지 마.\n");
        prompt.append("- 코드명, 프로시저명, 파일경로는 title이 아닌 description에 넣어.\n\n");
        prompt.append("- 이미 등록된 항목과 동일한 내용을 추출하지 마.\n");
        prompt.append("- 추출할 항목이 없으면 빈 배열 []을 반환해.\n\n");
        prompt.append("- \"X\", \"안함\", \"제외\" 등 취소/제외된 항목은 추출하지 마.\n");
        prompt.append("- 동일한 내용을 다른 표현으로 중복 추출하지 마.\n");
        prompt.append("- 업무 환경, 사용 장비, 원칙/규칙 등은 업무가 아니므로 추출하지 마.\n");

        // 7. 응답 형식
        prompt.append("=== 응답 형식 (JSON 배열) ===\n");
        prompt.append("[\n");
        prompt.append("  {\n");
        prompt.append("    \"type\": \"WORK 또는 SCHEDULE\",\n");
        prompt.append("    \"title\": \"제목\",\n");
        prompt.append("    \"description\": \"설명 (없으면 null)\",\n");
        prompt.append("    \"dueDate\": \"YYYY-MM-DD (WORK일 때 마감일, 없으면 null)\",\n");
        prompt.append("    \"startDateTime\": \"YYYY-MM-DDThh:mm (SCHEDULE일 때 시작 시간, 없으면 null)\",\n");
        prompt.append("    \"category\": \"WORK, EXERCISE, OTHER 중 하나\",\n");
        prompt.append("    \"status\": \"PENDING 또는 COMPLETED\"\n");
        prompt.append("  }\n");
        prompt.append("]\n");

        return prompt.toString();
    }

    public String taskListToString(List<Task> taskList) {

        StringBuilder sb = new StringBuilder();
        for (Task task : taskList) {
            sb.append(String.format("- [%s] %s", task.getType(), task.getTitle()));
            if (task.getDescription() != null) {
                sb.append(String.format(" (%s)", task.getDescription()));
            }
            if (task.getDueDate() != null) {
                sb.append(String.format(" 마감: %s", task.getDueDate()));
            }
            sb.append(String.format(" [%s]", task.getCategory()));
            sb.append("\n");
        }
        return sb.toString();
    }

    public List<ParsingResult> parseGeminiResponse(String response, List<Task> registeredTasks) {

        try {
            JsonNode rootNode = objectMapper.readTree(response);
            List<ParsingResult> results = new ArrayList<>();

            for (JsonNode node : rootNode) {
                ParsingResult result = ParsingResult.builder()
                        .type(nullCheckText(node, "type"))
                        .title(nullCheckText(node, "title"))
                        .description(nullCheckText(node, "description"))
                        .dueDate(nullCheckText(node, "dueDate"))
                        .startDateTime(nullCheckText(node, "startDateTime"))
                        .category(nullCheckText(node, "category"))
                        .status(nullCheckText(node, "status"))
                        .build();

                if (!isDuplicate(result, registeredTasks)) {
                    results.add(result);
                }
            }

            return results;
        } catch (Exception e) {
            log.error("Failed to parse Gemini response: {}", e.getMessage());
            return List.of();
        }
    }

    private String nullCheckText(JsonNode node, String field) {
        if (!node.has(field) || node.get(field).isNull()) return null;
        String value = node.get(field).asText();
        return "null".equals(value) || value.isBlank() ? null : value;
    }

    private boolean isDuplicate(ParsingResult result, List<Task> registeredTasks) {
        return registeredTasks.stream().anyMatch(task ->
                task.getTitle().equalsIgnoreCase(result.getTitle())
        );
    }

    private Map<String, Object> buildSchema() {

        Map<String, Object> itemSchema = new java.util.HashMap<>();
        itemSchema.put("type", "OBJECT");
        itemSchema.put("properties", Map.of(
                "type", Map.of("type", "STRING"),
                "title", Map.of("type", "STRING"),
                "description", Map.of("type", "STRING"),
                "dueDate", Map.of("type", "STRING"),
                "startDateTime", Map.of("type", "STRING"),
                "category", Map.of("type", "STRING"),
                "status", Map.of("type", "STRING")
        ));
        itemSchema.put("required", List.of("type", "title", "category", "status"));

        Map<String, Object> responseSchema = new java.util.HashMap<>();
        responseSchema.put("type", "ARRAY");
        responseSchema.put("items", itemSchema);

        return responseSchema;
    }
}
