package com.example.novaai.controller;

import com.example.novaai.dto.ApiResponse;
import com.example.novaai.dto.MessageResponse;
import com.example.novaai.dto.SendMessageRequest;
import com.example.novaai.security.SecurityUtils;
import com.example.novaai.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RestController
@RequestMapping("/api/conversations/{conversationId}/messages")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Messages", description = "Send messages and stream AI responses")
public class MessageController {
    private final MessageService messageService;

    @PostMapping
    @Operation(summary = "Send a message and receive a complete AI response")
    public ApiResponse<MessageResponse> send(
        @PathVariable UUID conversationId,
        @Valid @RequestBody SendMessageRequest request
    ) {
        return ApiResponse.success(messageService.sendMessage(
            SecurityUtils.getCurrentUserId(), conversationId, request));
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream an AI response as server-sent events")
    public Flux<ServerSentEvent<String>> stream(
        @PathVariable UUID conversationId,
        @Valid @RequestBody SendMessageRequest request
    ) {
        return messageService.streamMessage(
                SecurityUtils.getCurrentUserId(), conversationId, request)
            .map(token -> ServerSentEvent.<String>builder()
                .event("token").data(token).build())
            .concatWithValues(ServerSentEvent.<String>builder()
                .event("done").data("[DONE]").build())
            .onErrorResume(error -> Flux.just(ServerSentEvent.<String>builder()
                .event("error").data("Unable to complete the AI response").build()));
    }
}\n
