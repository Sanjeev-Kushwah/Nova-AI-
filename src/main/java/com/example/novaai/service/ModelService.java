package com.example.novaai.service;

import com.example.novaai.dto.ModelResponse;
import com.example.novaai.integration.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModelService {

    private final AIService aiService;

    public List<ModelResponse> getAvailableModels() {
        // These are the models the frontend can select. In a production system
        // this could be fetched from the provider's /models endpoint.
        return List.of(
            new ModelResponse("gpt-4o", "Nova Pro", "Most capable model for complex reasoning"),
            new ModelResponse("gpt-4o-mini", "Nova Standard", "Balanced speed and intelligence"),
            new ModelResponse("gpt-3.5-turbo", "Nova Mini", "Fast and efficient for everyday tasks")
        );
    }

    public String getDefaultModel() {
        return aiService.getDefaultModel();
    }
}
