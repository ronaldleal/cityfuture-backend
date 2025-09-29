package com.cityfuture.api.exception;

import com.cityfuture.api.dto.ErrorResponse;
import com.cityfuture.domain.exception.InsufficientMaterialException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaterialNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMaterialNotFound(MaterialNotFoundException ex) {
        ErrorResponse error = new ErrorResponse("MATERIAL_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MaterialAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleMaterialAlreadyExists(
            MaterialAlreadyExistsException ex) {
        ErrorResponse error = new ErrorResponse("MATERIAL_ALREADY_EXISTS", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(InsufficientMaterialException.class)
    public ResponseEntity<Object> handleInsufficientMaterialException(
            InsufficientMaterialException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Material Insuficiente");
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Tipo de Construcción Inválido");
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Error de Validación");
        
        List<String> errors = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.add(error.getField() + ": " + error.getDefaultMessage())
        );
        ex.getBindingResult().getGlobalErrors().forEach(error ->
            errors.add(error.getObjectName() + ": " + error.getDefaultMessage())
        );
        
        body.put("validationErrors", errors);
        body.put("message", "Los datos proporcionados no son válidos");
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Error de Validación de Parámetros");
        
        List<String> errors = new ArrayList<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.add(violation.getPropertyPath() + ": " + violation.getMessage());
        }
        
        body.put("validationErrors", errors);
        body.put("message", "Los parámetros proporcionados no son válidos");
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Recurso No Encontrado");
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }
}
