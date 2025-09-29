package com.cityfuture.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Autenticación", description = "API para autenticación y autorización de usuarios")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Data
    public static class LoginRequest {
        @NotBlank(message = "El nombre de usuario es obligatorio")
        private String username;
        
        @NotBlank(message = "La contraseña es obligatoria")
        private String password;
    }

    @Data
    public static class LoginResponse {
        private String token;
        private String type = "Bearer";
        private String username;
        private String role;
        private long expiresIn;
    }

    @Operation(summary = "Iniciar sesión", 
               description = "Autentica un usuario y devuelve un token JWT para acceder a los endpoints protegidos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login exitoso",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = LoginResponse.class),
                    examples = @ExampleObject(value = "{\"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\", \"type\": \"Bearer\", \"username\": \"arquitecto\", \"role\": \"ARQUITECTO\", \"expiresIn\": 86400}"))),
        @ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        // TODO: Implementar lógica de autenticación real
        // Por ahora, solo para documentación de Swagger
        
        if ("arquitecto".equals(loginRequest.getUsername()) && "password".equals(loginRequest.getPassword())) {
            LoginResponse response = new LoginResponse();
            response.setToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhcnF1aXRlY3RvIiwicm9sZSI6IkFSUVVJVEVDVE8iLCJpYXQiOjE2MzUxNzUyMDAsImV4cCI6MTYzNTI2MTYwMH0.example");
            response.setUsername(loginRequest.getUsername());
            response.setRole("ARQUITECTO");
            response.setExpiresIn(86400); // 24 horas
            return ResponseEntity.ok(response);
        }
        
        return ResponseEntity.status(401)
                .body(Map.of("error", "Credenciales inválidas", 
                           "message", "Usuario o contraseña incorrectos"));
    }

    @Operation(summary = "Cerrar sesión", 
               description = "Invalida el token JWT actual (implementación pendiente)")
    @ApiResponse(responseCode = "200", description = "Sesión cerrada exitosamente")
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // TODO: Implementar lógica de logout (blacklist de tokens)
        return ResponseEntity.ok(Map.of("message", "Sesión cerrada exitosamente"));
    }

    @Operation(summary = "Validar token", 
               description = "Valida si el token JWT proporcionado es válido")
    @ApiResponse(responseCode = "200", description = "Token válido")
    @ApiResponse(responseCode = "401", description = "Token inválido o expirado")
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken() {
        // TODO: Implementar validación de token
        return ResponseEntity.ok(Map.of("valid", true, "message", "Token válido"));
    }
}