package com.rednorte.auth_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth

                // 🔓 permitir login
                .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()

                // 🔓 opcional (por seguridad déjalo igual)
                .requestMatchers("/auth/**").permitAll()

                // 🔒 todo lo demás protegido
                .anyRequest().authenticated()
            );

        return http.build();
    }

    // 🔐 encoder como bean (IMPORTANTE)
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}