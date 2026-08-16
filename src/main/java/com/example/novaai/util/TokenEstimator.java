package com.example.novaai.util;

import java.util.UUID;

public final class TokenEstimator {

    private TokenEstimator() {}

    /**
     * Rough token estimate — approximately 4 characters per token for English text.
     * This is a heuristic for usage tracking; the actual count from the AI provider
     * should be used when available.
     */
    public static int estimate(String text) {
        if (text == null || text.isEmpty()) return 0;
        return (int) Math.ceil(text.length() / 4.0);
    }

    public static int estimateMessages(java.util.List<com.example.novaai.integration.ChatMessage> messages) {
        return messages.stream()
            .mapToInt(m -> estimate(m.content()))
            .sum();
    }
}
