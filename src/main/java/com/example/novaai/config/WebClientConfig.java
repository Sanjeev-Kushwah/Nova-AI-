package com.example.novaai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient openAiWebClient(AppConfig appConfig) {
        var openai = appConfig.getAi().getOpenai();
        var httpClient = HttpClient.create()
            .responseTimeout(Duration.ofMillis(appConfig.getAi().getTimeoutMs()));

        return WebClient.builder()
            .baseUrl(openai.getBaseUrl())
            .defaultHeader("Authorization", "Bearer " + openai.getApiKey())
            .defaultHeader("Content-Type", "application/json")
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
            .build();
    }
}
