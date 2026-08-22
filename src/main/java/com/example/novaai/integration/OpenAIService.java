package com.example.novaai.integration;

import com.example.novaai.exception.AIServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
public class OpenAIService implements AIService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String defaultModel;

    public OpenAIService(WebClient webClient, ObjectMapper objectMapper, String defaultModel) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
        this.defaultModel = defaultModel;
    }

    @Override
    public AiResponse generateResponse(String model, List<ChatMessage> messages) {
        try {
            ObjectNode requestBody = buildRequestBody(model, messages, false);
            JsonNode response = webClient.post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

            if (response == null) {
                throw new AIServiceException("AI provider returned an empty response");
            }

            String content = response.path("choices").path(0).path("message").path("content").asText("");
            int inputTokens = response.path("usage").path("prompt_tokens").asInt(0);
            int outputTokens = response.path("usage").path("completion_tokens").asInt(0);

            return new AiResponse(content, inputTokens, outputTokens);
        } catch (WebClientResponseException e) {
            log.error("OpenAI API error: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AIServiceException("AI provider returned an error. Please try again.", e);
        } catch (AIServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new AIServiceException("Failed to communicate with AI provider.", e);
        }
    }

    @Override
    public Flux<String> streamResponse(String model, List<ChatMessage> messages) {
        ObjectNode requestBody = buildRequestBody(model, messages, true);

        return webClient.post()
            .uri("/chat/completions")
            .bodyValue(requestBody)
            .retrieve()
            .bodyToFlux(String.class)
            .filter(line -> !line.isBlank() && !line.equals("[DONE]"))
            .map(this::extractStreamContent)
            .filter(chunk -> chunk != null && !chunk.isEmpty())
            .onErrorResume(WebClientResponseException.class, e -> {
                log.error("OpenAI streaming error: status={}", e.getStatusCode());
                return Flux.error(new AIServiceException("AI streaming failed. Please try again.", e));
            })
            .onErrorResume(e -> {
                if (e instanceof AIServiceException) return Flux.error(e);
                return Flux.error(new AIServiceException("Connection to AI provider interrupted.", e));
            });
    }

    @Override
    public String getDefaultModel() {
        return defaultModel;
    }

    private ObjectNode buildRequestBody(String model, List<ChatMessage> messages, boolean stream) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", model);
        body.put("stream", stream);

        ArrayNode messagesArray = body.putArray("messages");
        for (ChatMessage msg : messages) {
            ObjectNode msgNode = messagesArray.addObject();
            msgNode.put("role", msg.role());
            msgNode.put("content", msg.content());
        }

        return body;
    }

    private String extractStreamContent(String line) {
        try {
            line = line.trim();
            if (line.startsWith("data:")) {
                line = line.substring(5).trim();
            }
            if (line.equals("[DONE]") || line.isBlank()) return null;

            JsonNode node = objectMapper.readTree(line);
            if (node.has("error")) {
                String message = node.path("error").path("message").asText("AI provider returned an error");
                throw new AIServiceException(message);
            }
            JsonNode delta = node.path("choices").path(0).path("delta");
            return delta.path("content").isTextual() ? delta.path("content").asText() : null;
        } catch (Exception e) {
            log.debug("Failed to parse SSE chunk: {}", line);
            return null;
        }
    }
}
