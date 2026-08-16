package com.example.novaai.mapper;

import com.example.novaai.dto.ConversationDetailResponse;
import com.example.novaai.dto.ConversationResponse;
import com.example.novaai.dto.MessageResponse;
import com.example.novaai.entity.Conversation;
import com.example.novaai.entity.Message;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConversationMapper {

    public ConversationResponse toResponse(Conversation conversation) {
        return new ConversationResponse(
            conversation.getId(),
            conversation.getTitle(),
            conversation.getCreatedAt(),
            conversation.getUpdatedAt()
        );
    }

    public ConversationDetailResponse toDetailResponse(
        Conversation conversation,
        List<Message> messages
    ) {
        List<MessageResponse> messageResponses = messages.stream()
            .map(this::toMessageResponse)
            .toList();
        return new ConversationDetailResponse(
            conversation.getId(),
            conversation.getTitle(),
            conversation.getCreatedAt(),
            conversation.getUpdatedAt(),
            messageResponses
        );
    }

    public MessageResponse toMessageResponse(Message message) {
        return new MessageResponse(
            message.getId(),
            message.getRole().name(),
            message.getContent(),
            message.getModel(),
            message.getTokenCount(),
            message.getCreatedAt()
        );
    }
}
