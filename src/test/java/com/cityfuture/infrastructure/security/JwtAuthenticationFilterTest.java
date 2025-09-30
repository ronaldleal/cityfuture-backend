package com.cityfuture.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UserDetails testUser;
    private String validToken;

    @BeforeEach
    void setUp() {
        testUser = new User(
            "testuser",
            "password",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";
        
        // Limpiar el contexto de seguridad antes de cada test
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_ValidTokenAndUser_SetsAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(request.getRequestURI()).thenReturn("/api/constructions");
        when(jwtService.extractUsername(validToken)).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(testUser);
        when(jwtService.isTokenValid(validToken, testUser)).thenReturn(true);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("testuser", SecurityContextHolder.getContext().getAuthentication().getName());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities()
            .contains(new SimpleGrantedAuthority("ROLE_USER")));

        verify(jwtService).extractUsername(validToken);
        verify(userDetailsService).loadUserByUsername("testuser");
        verify(jwtService).isTokenValid(validToken, testUser);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_NoAuthorizationHeader_DoesNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/constructions");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(jwtService, never()).extractUsername(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_InvalidAuthorizationHeader_DoesNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Invalid header");
        when(request.getRequestURI()).thenReturn("/api/constructions");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(jwtService, never()).extractUsername(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_InvalidToken_DoesNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(request.getRequestURI()).thenReturn("/api/constructions");
        when(jwtService.extractUsername(validToken)).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(testUser);
        when(jwtService.isTokenValid(validToken, testUser)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(jwtService).extractUsername(validToken);
        verify(userDetailsService).loadUserByUsername("testuser");
        verify(jwtService).isTokenValid(validToken, testUser);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_JwtServiceThrowsException_ClearsSecurityContext() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(request.getRequestURI()).thenReturn("/api/constructions");
        when(jwtService.extractUsername(validToken)).thenThrow(new RuntimeException("Invalid JWT"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(jwtService).extractUsername(validToken);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_UserDetailsServiceThrowsException_ClearsSecurityContext() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(request.getRequestURI()).thenReturn("/api/constructions");
        when(jwtService.extractUsername(validToken)).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser"))
            .thenThrow(new RuntimeException("User not found"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(jwtService).extractUsername(validToken);
        verify(userDetailsService).loadUserByUsername("testuser");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_AlreadyAuthenticated_DoesNotOverrideAuthentication() throws ServletException, IOException {
        // Arrange
        // Pre-establecer una autenticación en el contexto
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken existingAuth = 
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "existinguser", null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(request.getRequestURI()).thenReturn("/api/constructions");
        when(jwtService.extractUsername(validToken)).thenReturn("testuser");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertEquals("existinguser", SecurityContextHolder.getContext().getAuthentication().getName());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities()
            .contains(new SimpleGrantedAuthority("ROLE_ADMIN")));

        verify(jwtService).extractUsername(validToken);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotFilter_AuthEndpoint_ReturnsTrue() throws ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/auth/login");

        // Act
        boolean shouldNotFilter = jwtAuthenticationFilter.shouldNotFilter(request);

        // Assert
        assertTrue(shouldNotFilter);
    }

    @Test
    void shouldNotFilter_SwaggerEndpoint_ReturnsTrue() throws ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/swagger-ui/index.html");

        // Act
        boolean shouldNotFilter = jwtAuthenticationFilter.shouldNotFilter(request);

        // Assert
        assertTrue(shouldNotFilter);
    }

    @Test
    void shouldNotFilter_ApiDocsEndpoint_ReturnsTrue() throws ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/v3/api-docs");

        // Act
        boolean shouldNotFilter = jwtAuthenticationFilter.shouldNotFilter(request);

        // Assert
        assertTrue(shouldNotFilter);
    }

    @Test
    void shouldNotFilter_HealthEndpoint_ReturnsTrue() throws ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/actuator/health");

        // Act
        boolean shouldNotFilter = jwtAuthenticationFilter.shouldNotFilter(request);

        // Assert
        assertTrue(shouldNotFilter);
    }

    @Test
    void shouldNotFilter_RootEndpoint_ReturnsTrue() throws ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/");

        // Act
        boolean shouldNotFilter = jwtAuthenticationFilter.shouldNotFilter(request);

        // Assert
        assertTrue(shouldNotFilter);
    }

    @Test
    void shouldNotFilter_ProtectedEndpoint_ReturnsFalse() throws ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/constructions");

        // Act
        boolean shouldNotFilter = jwtAuthenticationFilter.shouldNotFilter(request);

        // Assert
        assertFalse(shouldNotFilter);
    }

    @Test
    void shouldNotFilter_MaterialsEndpoint_ReturnsFalse() throws ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/materials");

        // Act
        boolean shouldNotFilter = jwtAuthenticationFilter.shouldNotFilter(request);

        // Assert
        assertFalse(shouldNotFilter);
    }

    @Test
    void doFilterInternal_EmptyToken_DoesNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer ");
        when(request.getRequestURI()).thenReturn("/api/constructions");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(jwtService, never()).extractUsername(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_NullUsername_DoesNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(request.getRequestURI()).thenReturn("/api/constructions");
        when(jwtService.extractUsername(validToken)).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(jwtService).extractUsername(validToken);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_EmptyUsername_DoesNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(request.getRequestURI()).thenReturn("/api/constructions");
        when(jwtService.extractUsername(validToken)).thenReturn("");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(jwtService).extractUsername(validToken);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }
}