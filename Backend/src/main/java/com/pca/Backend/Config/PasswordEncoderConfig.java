package com.pca.Backend.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration séparée pour éviter une dépendance circulaire :
 * {@code SecurityConfig} → {@code CustomUserDetails} → {@code PasswordEncoder}
 * ne peut pas être défini dans la même classe que {@code SecurityConfig}.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
