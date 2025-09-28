package com.cityfuture.api.exception;

public class MaterialAlreadyExistsException extends RuntimeException {
    public MaterialAlreadyExistsException(String message) {
        super(message);
    }
}