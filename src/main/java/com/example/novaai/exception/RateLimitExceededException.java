package com.example.novaai.exception;

public class RateLimitExceededException extends AppException {
    public RateLimitExceededException(String message) {
        super("RATE_LIMIT_EXCEEDED", message);
    }
}
