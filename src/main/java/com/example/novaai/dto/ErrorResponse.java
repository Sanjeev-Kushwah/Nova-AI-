package com.example.novaai.dto;

public record ErrorResponse(
    boolean success,
    ErrorDetail error
) {
    public record ErrorDetail(
        String code,
        String message
    ) {}

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(false, new ErrorDetail(code, message));
    }
}
