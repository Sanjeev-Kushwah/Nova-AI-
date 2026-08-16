package com.example.novaai.controller;

import com.example.novaai.dto.ApiResponse;
import com.example.novaai.dto.ModelResponse;
import com.example.novaai.service.ModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/models")
@RequiredArgsConstructor
@Tag(name = "Models", description = "Available AI models")
public class ModelController {
    private final ModelService modelService;

    @GetMapping
    @Operation(summary = "List available AI models")
    public ApiResponse<List<ModelResponse>> list() {
        return ApiResponse.success(modelService.getAvailableModels());
    }
}

