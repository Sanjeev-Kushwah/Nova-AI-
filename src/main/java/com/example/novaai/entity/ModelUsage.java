package com.example.novaai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "model_usage",
    indexes = {
        @Index(name = "idx_model_usage_user_id", columnList = "user_id"),
        @Index(name = "idx_model_usage_conversation_id", columnList = "conversation_id"),
        @Index(name = "idx_model_usage_created_at", columnList = "created_at"),
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModelUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Column(name = "model", nullable = false, length = 100)
    private String model;

    @Column(name = "input_tokens", nullable = false)
    @Builder.Default
    private int inputTokens = 0;

    @Column(name = "output_tokens", nullable = false)
    @Builder.Default
    private int outputTokens = 0;

    @Column(name = "total_tokens", nullable = false)
    @Builder.Default
    private int totalTokens = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
