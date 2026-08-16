package com.example.novaai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(
    @NotBlank @Size(max = 8000) String content,
    @NotBlank @Size(max = 100) String model
) {}
