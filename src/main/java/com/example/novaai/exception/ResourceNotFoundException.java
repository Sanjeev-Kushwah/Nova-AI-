package com.example.novaai.exception;

public class ResourceNotFoundException extends AppException {
    public ResourceNotFoundException(String resource, String identifier) {
        super("RESOURCE_NOT_FOUND", resource + " not found: " + identifier);
    }
}
