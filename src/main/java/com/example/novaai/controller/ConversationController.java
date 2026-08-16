package com.example.novaai.controller;

import com.example.novaai.dto.ApiResponse;
import com.example.novaai.dto.ConversationDetailResponse;
import com.example.novaai.dto.ConversationResponse;
import com.example.novaai.dto.CreateConversationRequest;
import com.example.novaai.dto.RenameConversationRequest;
import com.example.novaai.security.SecurityUtils;
import com.example.novaai.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Conversations", description = "Create, list, rename, and delete conversations")
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping
    @Operation(summary = "Create a new conversation")
    public ResponseEntity<ApiResponse<ConversationResponse>> create(
        @Valid @RequestBody CreateConversationRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            conversationService.create(SecurityUtils.getCurrentUserId(), request)
        ));
    }

    @GetMapping
    @Operation(summary = "List all conversations for the current user")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(
            conversationService.listByUser(SecurityUtils.getCurrentUserId())
        ));
    }

    @GetMapping("/{conversationId}")
    @Operation(summary = "Get a conversation with all its messages")
    public ResponseEntity<ApiResponse<ConversationDetailResponse>> getById(
        @PathVariable UUID conversationId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            conversationService.getById(SecurityUtils.getCurrentUserId(), conversationId)
        ));
    }

    @PatchMapping("/{conversationId}")
    @Operation(summary = "Rename a conversation")
    public ResponseEntity<ApiResponse<ConversationResponse>> rename(
        @PathVariable UUID conversationId,
        @Valid @RequestBody RenameConversationRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            conversationService.rename(SecurityUtils.getCurrentUserId(), conversationId, request)
        ));
    }

    @DeleteMapping("/{conversationId}")
    @Operation(summary = "Delete a conversation and all its messages")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID conversationId) {
        conversationService.delete(SecurityUtils.getCurrentUserId(), conversationId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
