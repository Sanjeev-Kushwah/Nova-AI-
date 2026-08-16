package com.example.novaai.repository;

import com.example.novaai.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    List<Message> findByConversationIdOrderByCreatedAtAsc(UUID conversationId);

    List<Message> findTopNByConversationIdOrderByCreatedAtDesc(
        UUID conversationId,
        int limit
    );

    // Spring Data JPA requires a derived query name; we use a native-style limit
    // via @Query in service or Pageable. For simplicity, use Pageable in service.
}
