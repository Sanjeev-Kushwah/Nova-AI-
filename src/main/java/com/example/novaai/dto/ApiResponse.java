package com.example.novaai.dto;

public record ApiResponse<T>(
    boolean success,
    T data
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data);
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(true, null);
    }
}
