package com.example.novaai.controller;

import com.example.novaai.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {
    @GetMapping
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.success(Map.of(
            "status", "UP",
            "timestamp", Instant.now().toString()
        ));
    }
}

