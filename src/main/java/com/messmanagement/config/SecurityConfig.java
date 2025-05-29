package com.messmanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // For CSRF
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // Enables Spring Security's web security support
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Using BCrypt for strong password hashing
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for now (common for stateless APIs, but review during security hardening)
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                    .requestMatchers("/api/v1/auth/**").permitAll() // Allow public access to auth endpoints (register, login)
                    // .requestMatchers("/h2-console/**").permitAll() // Allow access to H2 console during development
                    .anyRequest().authenticated() // All other requests require authentication
            );
            // .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin())); // For H2 console if Spring Security blocks it

        // We will add JWT filter configuration here later

        return http.build();
    }
}
