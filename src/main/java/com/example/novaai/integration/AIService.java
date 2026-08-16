package com.example.novaai.integration;

import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Abstraction over an AI provider (OpenAI, Anthropic, local LLM, etc.).
 * Controllers and services must never reference a specific provider directly;
 * they only depend on this interface. The active implementation is selected
 * via the {@code app.ai.provider} configuration property.
 */
public interface AIService {

    /**
     * Generates a complete (non-streamed) AI response.
     *
     * @param model     the model identifier to use
     * @param messages  the full conversation context (system, user, assistant messages in order)
     * @return the AI response with content and token usage
     */
    AiResponse generateResponse(String model, List<ChatMessage> messages);

    /**
     * Streams an AI response token-by-token.
     *
     * @param model     the model identifier to use
     * @param messages  the full conversation context
     * @return a Flux emitting each content chunk as it arrives, followed by completion
     */
    Flux<String> streamResponse(String model, List<ChatMessage> messages);

    /**
     * @return the default model identifier for this provider
     */
    String getDefaultModel();
}
