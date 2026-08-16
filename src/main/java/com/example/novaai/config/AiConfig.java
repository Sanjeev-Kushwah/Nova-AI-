package com.example.novaai.config;

import com.example.novaai.integration.AIService;
import com.example.novaai.integration.OpenAIService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class AiConfig {

    private final AppConfig appConfig;
    private final ObjectMapper objectMapper;

    @Bean
    public AIService aiService(WebClient openAiWebClient) {
        String provider = appConfig.getAi().getProvider();
        return switch (provider.toLowerCase()) {
            case "openai" -> new OpenAIService(
                openAiWebClient,
                objectMapper,
                appConfig.getAi().getOpenai().getDefaultModel()
            );
            default -> throw new IllegalStateException("Unsupported AI provider: " + provider);
        };
    }
}
