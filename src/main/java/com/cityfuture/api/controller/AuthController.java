package com.cityfuture.api.controller;

import com.cityfuture.infrastructure.security.CustomUserDetailsService;
import com.cityfuture.infrastructure.security.JwtService;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Tag(name = "Autenticación", description = "API para autenticación y autorización de usuarios")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String ERROR_KEY = "error";
    private static final String MESSAGE_KEY = "message";
    
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

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
    public ResponseEntity<Object> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Autenticar al usuario
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            // Cargar detalles del usuario
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
            
            // Generar token JWT
            String jwtToken = jwtService.generateToken(userDetails);
            
            // Extraer el rol principal del usuario
            String role = userDetails.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .map(authority -> authority.replace("ROLE_", ""))
                    .orElse("USER");
            
            // Preparar respuesta
            LoginResponse response = new LoginResponse();
            response.setToken(jwtToken);
            response.setUsername(userDetails.getUsername());
            response.setRole(role);
            response.setExpiresIn(86400); // 24 horas en segundos

            log.info("Usuario autenticado exitosamente: {}", loginRequest.getUsername());
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            log.warn("Intento de login fallido para usuario: {}", loginRequest.getUsername());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEY, "Credenciales inválidas");
            errorResponse.put(MESSAGE_KEY, "Usuario o contraseña incorrectos");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        } catch (Exception e) {
            log.error("Error durante el proceso de autenticación: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEY, "Error de autenticación");
            errorResponse.put(MESSAGE_KEY, "Ha ocurrido un error durante el proceso de login");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(summary = "Obtener usuarios disponibles", 
               description = "Lista los usuarios de prueba disponibles (solo para desarrollo)")
    @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente")
    @GetMapping("/users")
    public ResponseEntity<Map<String, String>> getAvailableUsers() {
        return ResponseEntity.ok(userDetailsService.getAvailableUsers());
    }

    @Operation(summary = "Validar token", 
               description = "Valida si el token JWT proporcionado es válido y devuelve información del usuario")
    @ApiResponse(responseCode = "200", description = "Token válido")
    @ApiResponse(responseCode = "401", description = "Token inválido o expirado")
    @PostMapping("/validate")
    public ResponseEntity<Object> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String username = jwtService.extractUsername(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                if (jwtService.isTokenValid(token, userDetails)) {
                    Map<String, Object> tokenInfo = jwtService.getTokenInfo(token);
                    return ResponseEntity.ok(tokenInfo);
                }
            }
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEY, "Token inválido");
            errorResponse.put(MESSAGE_KEY, "El token proporcionado no es válido o ha expirado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            
        } catch (Exception e) {
            log.error("Error validando token: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEY, "Error validando token");
            errorResponse.put(MESSAGE_KEY, "Ha ocurrido un error al validar el token");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(summary = "Cerrar sesión", 
               description = "Endpoint informativo para cerrar sesión (en JWT stateless el token se invalida en el cliente)")
    @ApiResponse(responseCode = "200", description = "Información sobre cierre de sesión")
    @PostMapping("/logout")
    public ResponseEntity<Object> logout() {
        Map<String, Object> response = new HashMap<>();
        response.put(MESSAGE_KEY, "Para cerrar sesión, elimine el token del localStorage en el cliente");
        response.put("info", "JWT es stateless, no se requiere invalidación en servidor");
        return ResponseEntity.ok(response);
    }
}