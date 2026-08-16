package com.example.novaai.service;

import com.example.novaai.config.AppConfig;
import com.example.novaai.dto.MessageResponse;
import com.example.novaai.dto.SendMessageRequest;
import com.example.novaai.entity.Conversation;
import com.example.novaai.entity.Message;
import com.example.novaai.entity.ModelUsage;
import com.example.novaai.entity.User;
import com.example.novaai.exception.AIServiceException;
import com.example.novaai.exception.ResourceNotFoundException;
import com.example.novaai.integration.AiResponse;
import com.example.novaai.integration.AIService;
import com.example.novaai.integration.ChatMessage;
import com.example.novaai.mapper.ConversationMapper;
import com.example.novaai.repository.MessageRepository;
import com.example.novaai.repository.ModelUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ModelUsageRepository modelUsageRepository;
    private final ConversationService conversationService;
    private final AIService aiService;
    private final AppConfig appConfig;
    private final ConversationMapper mapper;
    private final RateLimitService rateLimitService;

    @Transactional
    public MessageResponse sendMessage(UUID userId, UUID conversationId, SendMessageRequest request) {
        rateLimitService.checkRateLimit(userId);

        Conversation conversation = conversationService.getOwnedEntity(userId, conversationId);
        User user = conversation.getUser();

        // 1. Save user message
        Message userMessage = Message.builder()
            .conversation(conversation)
            .role(Message.Role.USER)
            .content(request.content())
            .build();
        userMessage = messageRepository.save(userMessage);

        // 2. Build conversation context
        List<ChatMessage> context = buildContext(conversation);

        // 3. Call AI
        AiResponse aiResponse = aiService.generateResponse(request.model(), context);

        // 4. Save assistant message
        Message assistantMessage = Message.builder()
            .conversation(conversation)
            .role(Message.Role.ASSISTANT)
            .content(aiResponse.content())
            .model(request.model())
            .tokenCount(aiResponse.outputTokens())
            .build();
        assistantMessage = messageRepository.save(assistantMessage);

        // 5. Track usage
        saveUsage(user, conversation, request.model(), aiResponse);

        log.info("Message sent: conversationId={}, userId={}, model={}, outputTokens={}",
            conversationId, userId, request.model(), aiResponse.outputTokens());

        return mapper.toMessageResponse(assistantMessage);
    }

    /**
     * Streams an AI response token-by-token via SSE.
     * The user message is saved immediately, then the AI response is streamed.
     * When the stream completes, the assistant message and usage are persisted.
     */
    public Flux<String> streamMessage(UUID userId, UUID conversationId, SendMessageRequest request) {
        rateLimitService.checkRateLimit(userId);

        return Flux.defer(() -> {
            // Save user message in a transactional context
            SaveResult saved = saveUserMessage(userId, conversationId, request);
            List<ChatMessage> context = buildContext(saved.conversation);

            Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();
            StringBuilder accumulated = new StringBuilder();

            aiService.streamResponse(request.model(), context)
                .doOnNext(chunk -> {
                    accumulated.append(chunk);
                    sink.tryEmitNext(chunk);
                })
                .doOnComplete(() -> {
                    persistAssistantMessage(
                        saved.conversation, saved.user, request.model(),
                        accumulated.toString(), context
                    );
                    sink.tryEmitComplete();
                })
                .doOnError(e -> {
                    log.error("Stream error for conversation {}: {}", conversationId, e.getMessage());
                    // Save partial response if any content was accumulated
                    if (!accumulated.isEmpty()) {
                        persistAssistantMessage(
                            saved.conversation, saved.user, request.model(),
                            accumulated.toString(), context
                        );
                    }
                    sink.tryEmitError(e);
                })
                .subscribe();

            return sink.asFlux();
        });
    }

    @Transactional
    protected SaveResult saveUserMessage(UUID userId, UUID conversationId, SendMessageRequest request) {
        Conversation conversation = conversationService.getOwnedEntity(userId, conversationId);
        Message userMessage = Message.builder()
            .conversation(conversation)
            .role(Message.Role.USER)
            .content(request.content())
            .build();
        messageRepository.save(userMessage);
        return new SaveResult(conversation, conversation.getUser());
    }

    @Transactional
    protected void persistAssistantMessage(
        Conversation conversation, User user, String model,
        String content, List<ChatMessage> context
    ) {
        if (content == null || content.isBlank()) return;

        Message assistantMessage = Message.builder()
            .conversation(conversation)
            .role(Message.Role.ASSISTANT)
            .content(content)
            .model(model)
            .tokenCount(com.example.novaai.util.TokenEstimator.estimate(content))
            .build();
        messageRepository.save(assistantMessage);

        int inputTokens = com.example.novaai.util.TokenEstimator.estimateMessages(context);
        int outputTokens = assistantMessage.getTokenCount();
        saveUsage(user, conversation, model,
            new AiResponse(content, inputTokens, outputTokens));

        log.info("Streaming message persisted: conversationId={}, model={}",
            conversation.getId(), model);
    }

    private List<ChatMessage> buildContext(Conversation conversation) {
        int maxHistory = appConfig.getAi().getMaxHistoryMessages();
        var pageable = PageRequest.of(0, maxHistory, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Message> recent = messageRepository.findByConversationIdOrderByCreatedAtAsc(
            conversation.getId()
        );

        // Take last N messages and reverse to chronological order
        int fromIndex = Math.max(0, recent.size() - maxHistory);
        List<Message> history = recent.subList(fromIndex, recent.size());

        List<ChatMessage> context = new ArrayList<>();
        // System prompt first
        String systemPrompt = appConfig.getAi().getSystemPrompt();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            context.add(ChatMessage.system(systemPrompt));
        }
        // Then conversation history in chronological order
        for (Message msg : history) {
            if (msg.getRole() == Message.Role.USER) {
                context.add(ChatMessage.user(msg.getContent()));
            } else if (msg.getRole() == Message.Role.ASSISTANT) {
                context.add(ChatMessage.assistant(msg.getContent()));
            }
        }
        return context;
    }

    private void saveUsage(User user, Conversation conversation, String model, AiResponse response) {
        ModelUsage usage = ModelUsage.builder()
            .user(user)
            .conversation(conversation)
            .model(model)
            .inputTokens(response.inputTokens())
            .outputTokens(response.outputTokens())
            .totalTokens(response.totalTokens())
            .build();
        modelUsageRepository.save(usage);
    }

    private record SaveResult(Conversation conversation, User user) {}
}
