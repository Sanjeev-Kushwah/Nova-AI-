package com.example.novaai.exception;

public class ConflictException extends AppException {
    public ConflictException(String message) {
        super("CONFLICT", message);
    }
}
