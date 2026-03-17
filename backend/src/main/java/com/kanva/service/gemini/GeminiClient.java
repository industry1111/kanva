package com.kanva.service.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kanva.config.GeminiConfig;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Gemini API 클라이언트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiClient {

    private final RestTemplate geminiRestTemplate;
    private final GeminiConfig geminiConfig;


    public String generateJsonContent(String prompt,Map<String, Object> responseSchema) {
        String response = generateContent(prompt,responseSchema);
        return extractJsonContent(response);
    }
    /**
     * Gemini API에 텍스트 생성 요청
     *
     * @param prompt 프롬프트
     * @return 생성된 텍스트
     */
    private String generateContent(String prompt, Map<String, Object> responseSchema) {
        if (!geminiConfig.isConfigured()) {
            throw new IllegalStateException("Gemini API is not configured");
        }

        String url = String.format("%s/models/%s:generateContent?key=%s",
                geminiConfig.getBaseUrl(),
                geminiConfig.getModel(),
                geminiConfig.getApiKey());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> generationConfig = new java.util.HashMap<>();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("maxOutputTokens", 8192);
        generationConfig.put("topP", 0.95);
        generationConfig.put("responseMimeType", "application/json");

        generationConfig.put("responseSchema", responseSchema);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                ),
                "generationConfig", generationConfig
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<GeminiResponse> response = geminiRestTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    GeminiResponse.class
            );

            if (response.getBody() != null && response.getBody().getCandidates() != null
                    && !response.getBody().getCandidates().isEmpty()) {
                Candidate candidate = response.getBody().getCandidates().get(0);
                if (candidate.getContent() != null && candidate.getContent().getParts() != null
                        && !candidate.getContent().getParts().isEmpty()) {
                    return candidate.getContent().getParts().get(0).getText();
                }
            }

            log.error("Gemini API returned empty response");
            throw new RuntimeException("Gemini API returned empty response");

        } catch (RestClientException e) {
            log.error("Gemini API call failed: {}", e.getMessage(), e);
            throw new RuntimeException("Gemini API call failed: " + e.getMessage(), e);
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
        // JSON 배열 응답 처리 (responseSchema ARRAY 타입)
        if (cleaned.startsWith("[")) {
            int start = cleaned.indexOf("[");
            int end = cleaned.lastIndexOf("]") + 1;
            if (end > start) {
                return cleaned.substring(start, end);
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

    public boolean isAvailable() {
        return geminiConfig.isConfigured();
    }

    // Gemini API Response DTOs
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeminiResponse {
        private List<Candidate> candidates;
        private UsageMetadata usageMetadata;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Candidate {
        private Content content;
        private String finishReason;
        private int index;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Content {
        private List<Part> parts;
        private String role;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Part {
        private String text;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UsageMetadata {
        @JsonProperty("promptTokenCount")
        private int promptTokenCount;
        @JsonProperty("candidatesTokenCount")
        private int candidatesTokenCount;
        @JsonProperty("totalTokenCount")
        private int totalTokenCount;
    }
}
