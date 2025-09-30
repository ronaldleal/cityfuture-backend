package com.cityfuture.infrastructure.security;


import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        
        // Configurar las propiedades usando reflection
        ReflectionTestUtils.setField(jwtService, "secretKey", "CityFutureSecretKeyForJWTTokenGeneration2025VerySecureAndLongKey123");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L); // 24 horas
        ReflectionTestUtils.setField(jwtService, "issuer", "CityFuture-Backend");

        // Crear usuario de prueba
        testUser = new User(
            "testuser",
            "password",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    void generateToken_ValidUser_ReturnsValidToken() {
        // Act
        String token = jwtService.generateToken(testUser);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(3, token.split("\\.").length); // JWT debe tener 3 partes separadas por puntos
    }

    @Test
    void generateToken_WithExtraClaims_ReturnsValidToken() {
        // Arrange
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "ADMIN");
        extraClaims.put("permissions", "READ_WRITE");

        // Act
        String token = jwtService.generateToken(extraClaims, testUser);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void extractUsername_ValidToken_ReturnsCorrectUsername() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        String extractedUsername = jwtService.extractUsername(token);

        // Assert
        assertEquals("testuser", extractedUsername);
    }

    @Test
    void extractUsername_InvalidToken_ThrowsException() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(MalformedJwtException.class, () -> {
            jwtService.extractUsername(invalidToken);
        });
    }

    @Test
    void isTokenValid_ValidTokenAndUser_ReturnsTrue() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        boolean isValid = jwtService.isTokenValid(token, testUser);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void isTokenValid_ValidTokenWrongUser_ReturnsFalse() {
        // Arrange
        String token = jwtService.generateToken(testUser);
        UserDetails differentUser = new User(
            "differentuser",
            "password",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // Act
        boolean isValid = jwtService.isTokenValid(token, differentUser);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void isTokenValid_ExpiredToken_ReturnsFalse() {
        // Arrange
        // Configurar un tiempo de expiración muy corto
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 1L); // 1 millisegundo
        String token = jwtService.generateToken(testUser);
        
        // Esperar para que el token expire usando un bucle
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 10) {
            // Esperar brevemente
        }

        // Act
        boolean isValid = jwtService.isTokenValid(token, testUser);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void isTokenValid_InvalidToken_ReturnsFalse() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        boolean isValid = jwtService.isTokenValid(invalidToken, testUser);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void getTokenInfo_ValidToken_ReturnsTokenInformation() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        Map<String, Object> tokenInfo = jwtService.getTokenInfo(token);

        // Assert
        assertNotNull(tokenInfo);
        assertEquals("testuser", tokenInfo.get("username"));
        assertEquals("CityFuture-Backend", tokenInfo.get("issuer"));
        assertNotNull(tokenInfo.get("issuedAt"));
        assertNotNull(tokenInfo.get("expiration"));
        assertFalse((Boolean) tokenInfo.get("isExpired"));
    }

    @Test
    void getTokenInfo_ExpiredToken_ReturnsTokenInfoWithExpiredTrue() {
        // Arrange
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 1L); // 1 millisegundo
        String token = jwtService.generateToken(testUser);
        
        // Esperar para que el token expire usando un bucle
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 10) {
            // Esperar brevemente
        }

        // Act
        Map<String, Object> tokenInfo = jwtService.getTokenInfo(token);

        // Assert
        assertNotNull(tokenInfo);
        assertEquals("testuser", tokenInfo.get("username"));
        assertTrue((Boolean) tokenInfo.get("isExpired"));
    }

    @Test
    void extractClaim_ValidToken_ReturnsCorrectClaim() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        String subject = jwtService.extractClaim(token, io.jsonwebtoken.Claims::getSubject);
        String issuer = jwtService.extractClaim(token, io.jsonwebtoken.Claims::getIssuer);

        // Assert
        assertEquals("testuser", subject);
        assertEquals("CityFuture-Backend", issuer);
    }

    @Test
    void extractClaim_InvalidToken_ThrowsException() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(MalformedJwtException.class, () -> {
            jwtService.extractClaim(invalidToken, io.jsonwebtoken.Claims::getSubject);
        });
    }

    @Test
    void generateToken_DifferentUsers_ReturnsDifferentTokens() {
        // Arrange
        UserDetails user1 = new User("user1", "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        UserDetails user2 = new User("user2", "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));

        // Act
        String token1 = jwtService.generateToken(user1);
        String token2 = jwtService.generateToken(user2);

        // Assert
        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2);
        assertEquals("user1", jwtService.extractUsername(token1));
        assertEquals("user2", jwtService.extractUsername(token2));
    }

    @Test
    void generateToken_SameUserMultipleTimes_ReturnsDifferentTokens() {
        // Arrange & Act
        String token1 = jwtService.generateToken(testUser);
        String token2 = jwtService.generateToken(testUser);

        // Assert
        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2); // Deberían ser diferentes debido a timestamps diferentes
        assertEquals("testuser", jwtService.extractUsername(token1));
        assertEquals("testuser", jwtService.extractUsername(token2));
    }

    @Test
    void isTokenValid_NullToken_ReturnsFalse() {
        // Act
        boolean isValid = jwtService.isTokenValid(null, testUser);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void isTokenValid_EmptyToken_ReturnsFalse() {
        // Act
        boolean isValid = jwtService.isTokenValid("", testUser);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void extractUsername_TamperedToken_ThrowsSignatureException() {
        // Arrange
        String validToken = jwtService.generateToken(testUser);
        String tamperedToken = validToken.substring(0, validToken.length() - 5) + "TAMPERED";

        // Act & Assert
        assertThrows(SignatureException.class, () -> {
            jwtService.extractUsername(tamperedToken);
        });
    }

    @Test
    void generateToken_WithNullExtraClaims_ReturnsValidToken() {
        // Act
        String token = jwtService.generateToken(null, testUser);

        // Assert
        assertNotNull(token);
        assertEquals("testuser", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, testUser));
    }

    @Test
    void generateToken_WithEmptyExtraClaims_ReturnsValidToken() {
        // Arrange
        Map<String, Object> emptyClaims = new HashMap<>();

        // Act
        String token = jwtService.generateToken(emptyClaims, testUser);

        // Assert
        assertNotNull(token);
        assertEquals("testuser", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, testUser));
    }
}