package com.example.novaai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    private Jwt jwt = new Jwt();
    private Cors cors = new Cors();
    private Ai ai = new Ai();
    private RateLimit rateLimit = new RateLimit();
    private Message message = new Message();

    @Getter @Setter
    public static class Jwt {
        private String secret = "";
        private long expirationMs = 86400000;
        private long refreshExpirationMs = 604800000;
    }

    @Getter @Setter
    public static class Cors {
        private String allowedOrigins = "http://localhost:3000";
        private String allowedMethods = "GET,POST,PUT,PATCH,DELETE,OPTIONS";
        private String allowedHeaders = "*";
        private boolean allowCredentials = true;
    }

    @Getter @Setter
    public static class Ai {
        private String provider = "openai";
        private String systemPrompt = "";
        private int maxHistoryMessages = 20;
        private long timeoutMs = 120000;
        private Openai openai = new Openai();
    }

    @Getter @Setter
    public static class Openai {
        private String apiKey = "";
        private String baseUrl = "https://api.openai.com/v1";
        private String defaultModel = "gpt-4o-mini";
    }

    @Getter @Setter
    public static class RateLimit {
        private int authenticatedRpm = 30;
        private int capacity = 30;
    }

    @Getter @Setter
    public static class Message {
        private int maxLength = 8000;
    }
}
