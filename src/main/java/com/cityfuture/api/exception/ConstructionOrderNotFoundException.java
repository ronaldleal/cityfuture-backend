package com.cityfuture.api.exception;

public class ConstructionOrderNotFoundException extends RuntimeException {
    public ConstructionOrderNotFoundException(String message) {
        super(message);
    }
}
