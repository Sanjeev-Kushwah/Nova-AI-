package com.example.novaai.integration;

public record AiResponse(
    String content,
    int inputTokens,
    int outputTokens
) {
    public int totalTokens() {
        return inputTokens + outputTokens;
    }
}
