package com.example.novaai.exception;

public class AIServiceException extends AppException {
    public AIServiceException(String message) {
        super("AI_SERVICE_ERROR", message);
    }

    public AIServiceException(String message, Throwable cause) {
        super("AI_SERVICE_ERROR", message);
        initCause(cause);
    }
}
