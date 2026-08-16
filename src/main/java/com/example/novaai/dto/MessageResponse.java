package com.example.novaai.dto;

import java.time.Instant;
import java.util.UUID;

public record MessageResponse(
    UUID id,
    String role,
    String content,
    String model,
    int tokenCount,
    Instant createdAt
) {}
