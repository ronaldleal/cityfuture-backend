package com.cityfuture.infrastructure.security;

import com.cityfuture.domain.User;
import com.cityfuture.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "encodedPassword", "USER");
        testUser.setId(1L);
    }

    @Test
    void loadUserByUsername_ExistingUser_ReturnsUserDetails() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        assertTrue(result.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
        assertTrue(result.isEnabled());
        assertTrue(result.isAccountNonExpired());
        assertTrue(result.isAccountNonLocked());
        assertTrue(result.isCredentialsNonExpired());

        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void loadUserByUsername_NonExistingUser_ThrowsUsernameNotFoundException() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
            UsernameNotFoundException.class,
            () -> customUserDetailsService.loadUserByUsername("nonexistent")
        );

        assertTrue(exception.getMessage().contains("Usuario no encontrado"));
        assertTrue(exception.getMessage().contains("nonexistent"));
        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    void loadUserByUsername_AdminUser_ReturnsAdminAuthorities() {
        // Arrange
        User adminUser = new User("admin", "adminPassword", "ADMIN");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername("admin");

        // Assert
        assertNotNull(result);
        assertEquals("admin", result.getUsername());
        assertTrue(result.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
        assertEquals(1, result.getAuthorities().size());
    }

    @Test
    void loadUserByUsername_ArquitectoUser_ReturnsArquitectoAuthorities() {
        // Arrange
        User arquitectoUser = new User("arquitecto", "arqPassword", "ARQUITECTO");
        when(userRepository.findByUsername("arquitecto")).thenReturn(Optional.of(arquitectoUser));

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername("arquitecto");

        // Assert
        assertNotNull(result);
        assertEquals("arquitecto", result.getUsername());
        assertTrue(result.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ARQUITECTO")));
        assertEquals(1, result.getAuthorities().size());
    }

    @Test
    void loadUserByUsername_NullUsername_ThrowsUsernameNotFoundException() {
        // Arrange
        when(userRepository.findByUsername(null)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            UsernameNotFoundException.class,
            () -> customUserDetailsService.loadUserByUsername(null)
        );

        verify(userRepository).findByUsername(null);
    }

    @Test
    void loadUserByUsername_EmptyUsername_ThrowsUsernameNotFoundException() {
        // Arrange
        when(userRepository.findByUsername("")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            UsernameNotFoundException.class,
            () -> customUserDetailsService.loadUserByUsername("")
        );

        verify(userRepository).findByUsername("");
    }

    @Test
    void getAvailableUsers_ReturnsUserInformation() {
        // Act
        Map<String, String> availableUsers = customUserDetailsService.getAvailableUsers();

        // Assert
        assertNotNull(availableUsers);
        assertEquals(3, availableUsers.size());
        
        assertTrue(availableUsers.containsKey("admin"));
        assertTrue(availableUsers.containsKey("arquitecto"));
        assertTrue(availableUsers.containsKey("user"));
        
        assertTrue(availableUsers.get("admin").contains("ROLE_ADMIN"));
        assertTrue(availableUsers.get("admin").contains("admin123"));
        
        assertTrue(availableUsers.get("arquitecto").contains("ROLE_ARQUITECTO"));
        assertTrue(availableUsers.get("arquitecto").contains("arq123"));
        
        assertTrue(availableUsers.get("user").contains("ROLE_USER"));
        assertTrue(availableUsers.get("user").contains("user123"));
    }

    @Test
    void initializeUsers_CreatesDefaultUsers() {
        // Arrange
        when(userRepository.existsByUsername("admin")).thenReturn(false);
        when(userRepository.existsByUsername("arquitecto")).thenReturn(false);
        when(userRepository.existsByUsername("user")).thenReturn(false);
        when(passwordEncoder.encode("admin123")).thenReturn("encodedAdmin123");
        when(passwordEncoder.encode("arq123")).thenReturn("encodedArq123");
        when(passwordEncoder.encode("user123")).thenReturn("encodedUser123");
        when(userRepository.count()).thenReturn(3L);

        // Act
        customUserDetailsService.initializeUsers();

        // Assert
        verify(userRepository).existsByUsername("admin");
        verify(userRepository).existsByUsername("arquitecto");
        verify(userRepository).existsByUsername("user");
        verify(userRepository, times(3)).save(any(User.class));
        verify(userRepository).count();
        
        verify(passwordEncoder).encode("admin123");
        verify(passwordEncoder).encode("arq123");
        verify(passwordEncoder).encode("user123");
    }

    @Test
    void initializeUsers_UsersAlreadyExist_DoesNotCreateDuplicates() {
        // Arrange
        when(userRepository.existsByUsername("admin")).thenReturn(true);
        when(userRepository.existsByUsername("arquitecto")).thenReturn(true);
        when(userRepository.existsByUsername("user")).thenReturn(true);
        when(userRepository.count()).thenReturn(3L);

        // Act
        customUserDetailsService.initializeUsers();

        // Assert
        verify(userRepository).existsByUsername("admin");
        verify(userRepository).existsByUsername("arquitecto");
        verify(userRepository).existsByUsername("user");
        verify(userRepository, never()).save(any(User.class));
        verify(userRepository).count();
        
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void initializeUsers_PartialUsersExist_CreatesOnlyMissingUsers() {
        // Arrange
        when(userRepository.existsByUsername("admin")).thenReturn(true);
        when(userRepository.existsByUsername("arquitecto")).thenReturn(false);
        when(userRepository.existsByUsername("user")).thenReturn(false);
        when(passwordEncoder.encode("arq123")).thenReturn("encodedArq123");
        when(passwordEncoder.encode("user123")).thenReturn("encodedUser123");
        when(userRepository.count()).thenReturn(3L);

        // Act
        customUserDetailsService.initializeUsers();

        // Assert
        verify(userRepository).existsByUsername("admin");
        verify(userRepository).existsByUsername("arquitecto");
        verify(userRepository).existsByUsername("user");
        verify(userRepository, times(2)).save(any(User.class));
        
        verify(passwordEncoder, never()).encode("admin123");
        verify(passwordEncoder).encode("arq123");
        verify(passwordEncoder).encode("user123");
    }

    @Test
    void loadUserByUsername_RepositoryThrowsException_PropagatesException() {
        // Arrange
        when(userRepository.findByUsername("testuser"))
            .thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> customUserDetailsService.loadUserByUsername("testuser")
        );

        assertEquals("Database connection error", exception.getMessage());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void serviceImplementsUserDetailsServiceInterface() {
        // Assert - Verify that CustomUserDetailsService implements UserDetailsService
        assertTrue(customUserDetailsService instanceof org.springframework.security.core.userdetails.UserDetailsService);
    }

    @Test
    void loadUserByUsername_MultipleCallsSameUser_ReturnsSameUserDetails() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails result1 = customUserDetailsService.loadUserByUsername("testuser");
        UserDetails result2 = customUserDetailsService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(result1.getUsername(), result2.getUsername());
        assertEquals(result1.getPassword(), result2.getPassword());
        assertEquals(result1.getAuthorities(), result2.getAuthorities());

        verify(userRepository, times(2)).findByUsername("testuser");
    }

    @Test
    void loadUserByUsername_CaseInsensitiveUsernameHandling() {
        // Arrange
        when(userRepository.findByUsername("TESTUSER")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            UsernameNotFoundException.class,
            () -> customUserDetailsService.loadUserByUsername("TESTUSER")
        );

        verify(userRepository).findByUsername("TESTUSER");
    }
}