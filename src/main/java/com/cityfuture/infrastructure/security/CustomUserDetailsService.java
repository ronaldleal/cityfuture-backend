package com.cityfuture.infrastructure.security;

import com.cityfuture.domain.User;
import com.cityfuture.infrastructure.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public CustomUserDetailsService(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void initializeUsers() {
        // Crear usuarios de prueba si no existen en la base de datos
        
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User("admin", passwordEncoder.encode("admin123"), "ADMIN");
            userRepository.save(admin);
            log.info("Created admin user");
        }
        
        if (!userRepository.existsByUsername("arquitecto")) {
            User arquitecto = new User("arquitecto", passwordEncoder.encode("arq123"), "ARQUITECTO");
            userRepository.save(arquitecto);
            log.info("Created arquitecto user");
        }
        
        if (!userRepository.existsByUsername("user")) {
            User user = new User("user", passwordEncoder.encode("user123"), "USER");
            userRepository.save(user);
            log.info("Created regular user");
        }

        log.info("User initialization completed. Total users: {}", userRepository.count());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", username);
                    return new UsernameNotFoundException("Usuario no encontrado: " + username);
                });
        
        log.debug("User loaded successfully: {}", username);
        return user;
    }

    /**
     * Método para obtener información sobre los usuarios disponibles (solo para desarrollo)
     */
    public Map<String, String> getAvailableUsers() {
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("admin", "ROLE_ADMIN - Password: admin123");
        userInfo.put("arquitecto", "ROLE_ARQUITECTO - Password: arq123");
        userInfo.put("user", "ROLE_USER - Password: user123");
        return userInfo;
    }
}