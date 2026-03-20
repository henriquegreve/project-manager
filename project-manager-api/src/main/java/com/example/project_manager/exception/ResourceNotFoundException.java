package com.example.project_manager.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Long id) {
        super(String.format("%s não encontrado com id: %d", resource, id));
    }
}
