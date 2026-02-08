package com.kanva.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@ConfigurationProperties(prefix = "gemini")
@Getter
@Setter
public class GeminiConfig {

    private String apiKey;
    private String model = "gemini-2.5-pro";
    private String baseUrl = "https://generativelanguage.googleapis.com/v1beta";
    private boolean enabled = false;

    @Bean
    public RestTemplate geminiRestTemplate() {
        return new RestTemplate();
    }

    public boolean isConfigured() {
        return enabled && apiKey != null && !apiKey.isBlank();
    }
}
