package com.example.novaai.service;

import com.example.novaai.dto.ConversationDetailResponse;
import com.example.novaai.dto.ConversationResponse;
import com.example.novaai.dto.CreateConversationRequest;
import com.example.novaai.dto.RenameConversationRequest;
import com.example.novaai.entity.Conversation;
import com.example.novaai.entity.Message;
import com.example.novaai.entity.User;
import com.example.novaai.exception.ResourceNotFoundException;
import com.example.novaai.mapper.ConversationMapper;
import com.example.novaai.repository.ConversationRepository;
import com.example.novaai.repository.MessageRepository;
import com.example.novaai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ConversationMapper mapper;

    @Transactional
    public ConversationResponse create(UUID userId, CreateConversationRequest request) {
        User user = userRepository.getReferenceById(userId);
        Conversation conversation = Conversation.builder()
            .user(user)
            .title(request.title() != null ? request.title() : "New Chat")
            .build();
        conversation = conversationRepository.save(conversation);
        log.info("Conversation created: id={}, userId={}", conversation.getId(), userId);
        return mapper.toResponse(conversation);
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> listByUser(UUID userId) {
        return conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
            .map(mapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public ConversationDetailResponse getById(UUID userId, UUID conversationId) {
        Conversation conversation = findOwnedConversation(userId, conversationId);
        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        return mapper.toDetailResponse(conversation, messages);
    }

    @Transactional
    public ConversationResponse rename(UUID userId, UUID conversationId, RenameConversationRequest request) {
        Conversation conversation = findOwnedConversation(userId, conversationId);
        conversation.setTitle(request.title());
        conversation = conversationRepository.save(conversation);
        log.info("Conversation renamed: id={}", conversationId);
        return mapper.toResponse(conversation);
    }

    @Transactional
    public void delete(UUID userId, UUID conversationId) {
        Conversation conversation = findOwnedConversation(userId, conversationId);
        conversationRepository.delete(conversation);
        log.info("Conversation deleted: id={}, userId={}", conversationId, userId);
    }

    /**
     * Internal method — returns the raw entity for the message service.
     */
    @Transactional(readOnly = true)
    public Conversation getOwnedEntity(UUID userId, UUID conversationId) {
        return findOwnedConversation(userId, conversationId);
    }

    private Conversation findOwnedConversation(UUID userId, UUID conversationId) {
        return conversationRepository.findByIdAndUserId(conversationId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Conversation", conversationId.toString()));
    }
}
